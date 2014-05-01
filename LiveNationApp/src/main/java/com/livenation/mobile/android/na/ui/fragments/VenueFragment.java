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
import android.support.v4.app.Fragment;
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
import com.livenation.mobile.android.na.app.ApiServiceBinder;
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
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Address;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SingleVenueParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.List;

import io.segment.android.models.Props;

public class VenueFragment extends LiveNationFragment implements SingleVenueView, EventsView, LiveNationMapFragment.MapReadyListener {
    public static final String PARAMETER_VENUE_ID = "venue_id";
    private static final float DEFAULT_MAP_ZOOM = 13f;
    private TextView venueTitle;
    private TextView location;
    private TextView telephone;
    private View venueInfo;
    private EventsView shows;
    private LiveNationMapFragment mapFragment;
    private GoogleMap map;
    private FavoriteCheckBox favoriteCheckBox;
    private LatLng mapLocationCache = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        Fragment showsFragment = ShowsListNonScrollingFragment.newInstance(ShowView.DisplayMode.VENUE);
        addFragment(R.id.fragment_venue_container_list, showsFragment, "shows");

        mapFragment = new LiveNationMapFragment();
        mapFragment.setMapReadyListener(this);

        addFragment(R.id.fragment_venue_map_container, mapFragment, "map");

        shows = (EventsView) showsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_venue, container,
                false);
        venueTitle = (TextView) result.findViewById(R.id.fragment_venue_title);

        location = (TextView) result.findViewById(R.id.venue_detail_location);
        telephone = (TextView) result.findViewById(R.id.venue_detail_telephone);
        venueInfo = result.findViewById(R.id.venue_detail_venue_info_link);
        favoriteCheckBox = (FavoriteCheckBox) result.findViewById(R.id.fragment_venue_favorite_checkbox);

        return result;
    }

    @Override
    public void setVenue(Venue venue) {
        //Analytics
        Props props = new Props();
        props.put("Venue Name", venue.getName());
        trackScreenWithLocation("User views VDP screen", props);

        venueTitle.setText(venue.getName());
        if (null != venue.getAddress()) {
            Address address = venue.getAddress();
            location.setText(address.getSmallFriendlyAddress(true));
        } else {
            location.setText("");
        }

        telephone.setText(venue.getFormattedPhoneNumber());

        if (venue.getBoxOffice() == null) {
            loadBoxOfficeInfo(venue.getNumericId());
        } else {
            displayBoxOfficeInfo(venue);
        }

        telephone.setOnClickListener(new OnPhoneNumberClick());
        location.setOnClickListener(new OnAddressClick(Double.parseDouble(venue.getLat()), Double.parseDouble(venue.getLng()), LiveNationApplication.get().getApplicationContext()));

        double lat = Double.valueOf(venue.getLat());
        double lng = Double.valueOf(venue.getLng());
        setMapLocation(lat, lng);

        favoriteCheckBox.bindToFavorite(Favorite.FAVORITE_VENUE, venue.getName(), venue.getNumericId(), getFavoritesPresenter());
    }

    @Override
    public void setEvents(List<Event> events) {
        shows.setEvents(events);
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

    ;

    private void loadBoxOfficeInfo(final long venueId) {
        LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                SingleVenueParameters parameters = new SingleVenueParameters();
                parameters.setVenueId(venueId);
                apiService.getSingleVenue(parameters, new ApiService.BasicApiCallback<Venue>() {
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

            @Override
            public void onApiServiceNotAvailable() {
                Log.e(getClass().getName(), "Could not load box office info. Api error");
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
            Intent intent = new Intent(getActivity(), VenueBoxOfficeActivity.class);
            intent.putExtras(VenueBoxOfficeActivity.getArguments(venue));
            startActivity(intent);
        }
    }

    private class OnPhoneNumberClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
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

        private OnAddressClick(double lat, double lng, Context context) {
            this.lat = lat;
            this.lng = lng;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            MapUtils.redirectToMapApplication(lat, lng, context);
        }
    }
}
