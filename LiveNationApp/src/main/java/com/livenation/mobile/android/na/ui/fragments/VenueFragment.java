/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.uber.UberClient;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.ui.VenueBoxOfficeActivity;
import com.livenation.mobile.android.na.ui.VenueShowsActivity;
import com.livenation.mobile.android.na.ui.dialogs.TravelListPopupWindow;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.na.ui.views.OverflowView;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.na.utils.ContactUtils;
import com.livenation.mobile.android.na.utils.MapUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Address;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SingleVenueParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.segment.android.models.Props;

import java.util.List;

public class VenueFragment extends LiveNationFragment implements SingleVenueView, EventsView, LiveNationMapFragment.MapReadyListener {
    private static final float DEFAULT_MAP_ZOOM = 13f;
    private static final int ACTIVITY_RESULT_UBER = 1;
    private final String SHOWS_FRAGMENT_TAG = "shows";
    private final String MAP_FRAGMENT_TAG = "maps";
    private UberClient uberClient;
    private TextView venueTitle;
    private TextView location;
    private TextView telephone;
    private ImageButton travelOptions;
    private View venueInfo;
    private View phonebox;
    private ShowsListNonScrollingFragment showsFragment;
    private LiveNationMapFragment mapFragment;
    private GoogleMap map;
    private FavoriteCheckBox favoriteCheckBox;
    private LatLng mapLocationCache = null;
    private final static int MAX_INLINE = 3;
    private Venue venue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uberClient = new UberClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_venue, container, false);
        venueTitle = (TextView) result.findViewById(R.id.fragment_venue_title);

        location = (TextView) result.findViewById(R.id.venue_detail_location);
        telephone = (TextView) result.findViewById(R.id.venue_detail_telephone);
        venueInfo = result.findViewById(R.id.venue_detail_venue_info_link);
        favoriteCheckBox = (FavoriteCheckBox) result.findViewById(R.id.fragment_venue_favorite_checkbox);
        phonebox = result.findViewById(R.id.venue_detail_phone_box);
        travelOptions = (ImageButton) result.findViewById(R.id.venue_travel_button);
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showsFragment = (ShowsListNonScrollingFragment) getChildFragmentManager().findFragmentByTag(SHOWS_FRAGMENT_TAG);
        if (showsFragment == null) {
            showsFragment = ShowsListNonScrollingFragment.newInstance(ShowView.DisplayMode.VENUE, AnalyticsCategory.VDP);
            addFragment(R.id.fragment_venue_container_list, showsFragment, SHOWS_FRAGMENT_TAG);
        }

        mapFragment = (LiveNationMapFragment) getChildFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = new LiveNationMapFragment();
            addFragment(R.id.fragment_venue_map_container, mapFragment, MAP_FRAGMENT_TAG);
        }
        mapFragment.setMapReadyListener(this);

        showsFragment.setMaxEvents(MAX_INLINE);
        showsFragment.setDisplayMode(ShowView.DisplayMode.VENUE);
        OverflowView showMoreView = new OverflowView(getActivity());
        showMoreView.setTitle(R.string.artist_events_overflow);

        showsFragment.setShowMoreItemsView(showMoreView);
    }

    @Override
    public void setVenue(Venue venue) {
        this.venue = venue;
        venueTitle.setText(venue.getName());
        if (null != venue.getAddress()) {
            Address address = venue.getAddress();
            location.setText(address.getSmallFriendlyAddress(true));
        } else {
            location.setText("");
        }

        String phoneNumber = venue.getFormattedPhoneNumber();
        telephone.setText(phoneNumber);
        if (phoneNumber.isEmpty()) {
            phonebox.setVisibility(View.GONE);
        } else {
            telephone.setOnClickListener(new OnPhoneNumberClick(venue));
        }

        if (venue.getBoxOffice() == null) {
            loadBoxOfficeInfo(venue.getNumericId());
        } else {
            displayBoxOfficeInfo(venue);
        }

        location.setOnClickListener(new OnAddressClick(venue, LiveNationApplication.get().getApplicationContext()));
        if (venue.getLat() != null && venue.getLng() != null) {
            double lat = Double.valueOf(venue.getLat());
            double lng = Double.valueOf(venue.getLng());
            setMapLocation(lat, lng);
            travelOptions.setOnClickListener(new OnTravelOptionsClick());
        } else {
            //hide travel options to unroutable venue
            travelOptions.setVisibility(View.GONE);
        }

        favoriteCheckBox.bindToFavorite(Favorite.fromVenue(venue), AnalyticsCategory.VDP);
        showsFragment.getShowMoreItemsView().setOnClickListener(new ShowAllEventsOnClickListener(venue));
    }

    @Override
    public void setEvents(List<Event> events) {
        showsFragment.setEvents(events);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        if (map != null) {
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            if (null != mapLocationCache) {
                setMapLocation(mapLocationCache.latitude, mapLocationCache.longitude);
            }
        } else {
            //TODO: Possible No Google play services installed
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case ACTIVITY_RESULT_UBER:
                LiveNationEstimate estimate = (LiveNationEstimate) data.getSerializableExtra(UberDialogFragment.EXTRA_RESULT_ESTIMATE);
                String productId = estimate.getProduct().getProductId();
                String address = venue.getAddress() != null ? venue.getAddress().getSmallFriendlyAddress(false) : "";
                Float lat = Float.valueOf(venue.getLat());
                Float lng = Float.valueOf(venue.getLng());
                Uri uri = uberClient.getUberLaunchUri(productId, lat, lng, venue.getName(), address);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                getActivity().startActivity(intent);
                break;
        }
    }

    private void loadBoxOfficeInfo(final long venueId) {
        SingleVenueParameters parameters = new SingleVenueParameters();
        parameters.setVenueId(venueId);
        LiveNationApplication.getLiveNationProxy().getSingleVenue(parameters, new BasicApiCallback<Venue>() {
            @Override
            public void onResponse(Venue fullVenue) {
                displayBoxOfficeInfo(fullVenue);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                Log.e(getClass().getName(), "Could not load box office info. " + error);
            }
        });
    }

    private void displayBoxOfficeInfo(Venue venue) {
        if (venue.getBoxOffice() == null || venue.getBoxOffice().isEmpty()) {
            venueInfo.setVisibility(View.GONE);
            venueInfo.setOnClickListener(null);
        } else {
            venueInfo.setVisibility(View.VISIBLE);

            OnVenueDetailClick onVenueClick = new OnVenueDetailClick(venue);
            venueInfo.setOnClickListener(onVenueClick);
        }
    }

    private void setMapLocation(double lat, double lng) {
        mapLocationCache = new LatLng(lat, lng);
        if (null == map) return;

        MarkerOptions marker = new MarkerOptions();
        marker.position(mapLocationCache);

        map.clear();
        map.addMarker(marker);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLocationCache, DEFAULT_MAP_ZOOM));
    }


    private class OnVenueDetailClick implements View.OnClickListener {
        private Venue venue;

        public OnVenueDetailClick(Venue venue) {
            this.venue = venue;
        }

        @Override
        public void onClick(View v) {
            Props props = new Props();
            props.put(AnalyticConstants.VENUE_NAME, venue.getName());
            props.put(AnalyticConstants.VENUE_ID, venue.getId());
            LiveNationAnalytics.track(AnalyticConstants.MORE_VENUE_INFO_TAP, AnalyticsCategory.VDP, props);

            Intent intent = new Intent(getActivity(), VenueBoxOfficeActivity.class);
            intent.putExtras(VenueBoxOfficeActivity.getArguments(venue));
            startActivity(intent);
        }
    }

    private class OnPhoneNumberClick implements View.OnClickListener {
        private Venue venue;

        public OnPhoneNumberClick(Venue venue) {
            this.venue = venue;
        }

        @Override
        public void onClick(View v) {
            Props props = new Props();
            props.put(AnalyticConstants.VENUE_NAME, venue.getName());
            props.put(AnalyticConstants.VENUE_ID, venue.getId());
            LiveNationAnalytics.track(AnalyticConstants.VENUE_PHONE_TAP, AnalyticsCategory.VDP, props);


            String phoneNumber = (String) VenueFragment.this.telephone.getText();
            phoneNumber.replace("[^0-9+]", "");
            if (phoneNumber != null || !phoneNumber.trim().isEmpty())
                ContactUtils.dial(phoneNumber, VenueFragment.this.getActivity());
        }
    }

    private class OnAddressClick implements View.OnClickListener {
        private double lat;
        private double lng;
        private Context context;
        private String address;
        private Venue venue;

        private OnAddressClick(Venue venue, Context context) {
            this.lat = Double.parseDouble(venue.getLat());
            this.lng = Double.parseDouble(venue.getLng());
            this.context = context;
            this.address = venue.getAddress().getSmallFriendlyAddress(false);
            this.venue = venue;
        }

        @Override
        public void onClick(View v) {
            Props props = new Props();
            props.put(AnalyticConstants.VENUE_NAME, venue.getName());
            props.put(AnalyticConstants.VENUE_ID, venue.getId());
            LiveNationAnalytics.track(AnalyticConstants.VENUE_ADDRESS_TAP, AnalyticsCategory.VDP, props);

            MapUtils.redirectToMapApplication(lat, lng, address, context);
        }
    }


    private class ShowAllEventsOnClickListener implements View.OnClickListener {
        private final Venue venue;

        private ShowAllEventsOnClickListener(Venue venue) {
            this.venue = venue;
        }

        @Override
        public void onClick(View view) {
            //Analytics
            Props props = new Props();
            props.put(AnalyticConstants.VENUE_NAME, venue.getName());
            props.put(AnalyticConstants.VENUE_ID, venue.getId());

            LiveNationAnalytics.track(AnalyticConstants.SEE_MORE_SHOWS_TAP, AnalyticsCategory.VDP, props);

            Intent intent = new Intent(getActivity(), VenueShowsActivity.class);
            intent.putExtras(VenueShowsActivity.getArguments(venue));
            startActivity(intent);
        }
    }

    private class OnTravelOptionsClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            TravelListPopupWindow popupWindow = new TravelListPopupWindow(getActivity(), travelOptions) {
                @Override
                public void onOptionClicked(TravelOption travelOption) {
                    switch (travelOption) {
                        case uber:
                            if (AnalyticsHelper.isAppInstalled(ExternalApplicationAnalytics.UBER.getPackageName(), getActivity())) {
                                //show uber price estimates
                                float lat = Float.valueOf(venue.getLat());
                                float lng = Float.valueOf(venue.getLng());
                                DialogFragment dialog = uberClient.getUberDialogFragment(lat, lng);
                                dialog.setTargetFragment(VenueFragment.this, ACTIVITY_RESULT_UBER);
                                dialog.show(getFragmentManager(), UberDialogFragment.UBER_DIALOG_TAG);
                            } else {
                                //no uber app installed, show sign up link
                                Intent intent = new Intent(Intent.ACTION_VIEW, uberClient.getUberSignupLink());
                                startActivity(intent);
                            }
                            break;

                        case maps:
                            location.performClick();
                            break;
                    }
                }
            };

            popupWindow.show();
        }
    }

}
