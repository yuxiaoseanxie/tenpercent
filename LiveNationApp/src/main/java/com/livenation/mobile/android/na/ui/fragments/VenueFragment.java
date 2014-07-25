/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.VenueBoxOfficeActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
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
    private final String SHOWS_FRAGMENT_TAG = "shows";
    private final String MAP_FRAGMENT_TAG = "maps";
    private TextView venueTitle;
    private TextView location;
    private TextView telephone;
    private View venueInfo;
    private View phonebox;
    private ShowsListNonScrollingFragment showsFragment;
    private LiveNationMapFragment mapFragment;
    private GoogleMap map;
    private FavoriteCheckBox favoriteCheckBox;
    private LatLng mapLocationCache = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_venue, container, false);
        venueTitle = (TextView) result.findViewById(R.id.fragment_venue_title);

        location = (TextView) result.findViewById(R.id.venue_detail_location);
        telephone = (TextView) result.findViewById(R.id.venue_detail_telephone);
        venueInfo = result.findViewById(R.id.venue_detail_venue_info_link);
        favoriteCheckBox = (FavoriteCheckBox) result.findViewById(R.id.fragment_venue_favorite_checkbox);
        phonebox = result.findViewById(R.id.venue_detail_phone_box);

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
    }

    @Override
    public void setVenue(Venue venue) {
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

        double lat = Double.valueOf(venue.getLat());
        double lng = Double.valueOf(venue.getLng());
        setMapLocation(lat, lng);

        favoriteCheckBox.bindToFavorite(Favorite.fromVenue(venue), AnalyticsCategory.VDP);
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
}
