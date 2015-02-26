/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LibraryErrorTracker;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.dialogs.CalendarDialogFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationMapFragment;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener.OnVenueFavoriteClick;
import com.livenation.mobile.android.na.ui.views.LineupView;
import com.livenation.mobile.android.na.ui.views.ShowVenueView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.ticketing.utils.OnThrottledClickListener;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

public class ShowFragment extends LiveNationFragment implements LiveNationMapFragment.MapReadyListener {
    private static final String EVENT = "com.livenation.mobile.android.na.ui.fragments.ShowFragment.EVENT";
    private static final String CALENDAR_DATE_FORMAT = "EEE MMM d'.' yyyy 'at' h:mm aa";
    private static final float DEFAULT_MAP_ZOOM = 13f;
    private final String MAP_FRAGMENT_TAG = "maps";
    private TextView calendarText;
    private ViewGroup calendarContainer;
    private ViewGroup lineupContainer;
    private ShowVenueView venueDetails;
    private GoogleMap map;
    private LiveNationMapFragment mapFragment;
    private LatLng mapLocationCache = null;
    private Event event;

    public static ShowFragment newInstance(Event event) {
        ShowFragment showFragment = new ShowFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EVENT, event);
        showFragment.setArguments(bundle);
        return showFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_show, container,
                false);
        lineupContainer = (ViewGroup) result.findViewById(R.id.fragment_show_artist_lineup_container);
        venueDetails = (ShowVenueView) result.findViewById(R.id.fragment_show_venue_details);
        calendarText = (TextView) result.findViewById(R.id.sub_show_calendar_text);
        calendarContainer = (ViewGroup) result.findViewById(R.id.sub_show_calendar_container);

        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Event event = (Event) getArguments().getSerializable(EVENT);
        setEvent(event);

        mapFragment = (LiveNationMapFragment) getChildFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = new LiveNationMapFragment();
            addFragment(R.id.fragment_show_map_container, mapFragment, MAP_FRAGMENT_TAG);
        }
        mapFragment.setMapReadyListener(this);
    }

    private void updateCalendar() {
        if (event.getIsMegaticket()) {
            calendarText.setText(getString(R.string.show_multiple_dates));
            calendarContainer.setOnClickListener(null);
            calendarContainer.findViewById(R.id.sub_show_calendar_plus_image).setVisibility(View.GONE);
        } else {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(CALENDAR_DATE_FORMAT, Locale.getDefault());
            TimeZone timeZone;
            if (event.getVenue().getTimeZone() != null) {
                timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
            } else {
                timeZone = TimeZone.getDefault();
            }
            dateFormatter.setTimeZone(timeZone);
            String calendarValue = dateFormatter.format(event.getLocalStartTime());
            calendarText.setText(calendarValue);
            OnCalendarViewClick onCalendarViewClick = new OnCalendarViewClick(event);
            calendarContainer.setOnClickListener(onCalendarViewClick);
        }
    }

    private void setEvent(Event event) {
        this.event = event;
        updateCalendar();
        updateVenue();
        updateLineup();

    }

    private void updateLineup() {
        lineupContainer.removeAllViews();
        for (Artist lineup : event.getLineup()) {
            LineupView view = new LineupView(getActivity());
            view.getTitle().setText(lineup.getName());

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lineupContainer.addView(view, layoutParams);

            view.bindToFavoriteArtist(lineup);

            view.setOnClickListener(new OnLineupViewClick(lineup, event));


            boolean lastItem = (event.getLineup().indexOf(lineup) == event.getLineup().size() - 1);
            if (lastItem) {
                view.getDivider().setVisibility(View.GONE);
            }

        }
    }

    private void updateVenue() {
        if (null != event.getVenue()) {
            Venue venue = event.getVenue();

            venueDetails.getTitle().setText(venue.getName());

            if (null != venue.getAddress()) {
                String address = venue.getAddress().getSmallFriendlyAddress(false);
                venueDetails.getLocation().setText(address);
            } else {
                venueDetails.getLocation().setText("");
            }

            venueDetails.getTelephone().setText(venue.getFormattedPhoneNumber());

            OnVenueDetailsClick onVenueClick = new OnVenueDetailsClick(event);
            venueDetails.setOnClickListener(onVenueClick);

            OnVenueFavoriteClick onVenueFavoriteClick = new OnVenueFavoriteClick(venue, AnalyticsCategory.SDP);
            venueDetails.getFavorite().setOnClickListener(onVenueFavoriteClick);
            venueDetails.getFavorite().bindToFavorite(Favorite.fromVenue(venue), AnalyticsCategory.SDP);

            //Longitude and Latitude can be null
            if (venue.getLat() != null && venue.getLng() != null) {
                double lat = Double.valueOf(venue.getLat());
                double lng = Double.valueOf(venue.getLng());
                setMapLocation(lat, lng);
            } else {
                new LibraryErrorTracker().track("This venue does not have a longitute and/or a latitude. Venue Id:" + venue.getId(), null);
            }
        } else {
            venueDetails.setOnClickListener(null);
        }
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
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    venueDetails.performClick();
                }
            });
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    venueDetails.performClick();
                    return true;
                }
            });
        } else {
            //TODO: Possible No Google play services installed
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

    private class OnVenueDetailsClick implements View.OnClickListener {
        private final Event event;

        public OnVenueDetailsClick(Event event) {
            this.event = event;
        }

        @Override
        public void onClick(View v) {
            Venue venue = event.getVenue();
            Intent intent = new Intent(getActivity(), VenueActivity.class);

            Bundle args = VenueActivity.getArguments(venue);
            intent.putExtras(args);

            //Analytics
            Props props = AnalyticsHelper.getPropsForEvent(event);
            props.put(AnalyticConstants.VENUE_ID, venue.getId());
            LiveNationAnalytics.track(AnalyticConstants.VENUE_CELL_TAP, AnalyticsCategory.SDP, props);

            startActivity(intent);
        }
    }

    private class OnLineupViewClick implements View.OnClickListener {
        private Artist lineupArtist;
        private Event event;

        public OnLineupViewClick(Artist lineupArtist, Event event) {
            this.lineupArtist = lineupArtist;
            this.event = event;
        }

        @Override
        public void onClick(View view) {
            //Analytics
            Props props = AnalyticsHelper.getPropsForEvent(event);
            props.put(AnalyticConstants.ARTIST_NAME, lineupArtist.getName());
            props.put(AnalyticConstants.ARTIST_ID, lineupArtist.getId());
            LiveNationAnalytics.track(AnalyticConstants.ARTIST_CELL_TAP, AnalyticsCategory.SDP, props);

            Intent intent = new Intent(getActivity(), ArtistActivity.class);

            Bundle args = ArtistActivity.getArguments(lineupArtist);
            intent.putExtras(args);

            startActivity(intent);
        }
    }

    private class OnCalendarViewClick extends OnThrottledClickListener {
        private CalendarDialogFragment dialogFragment;
        private Event event;

        public OnCalendarViewClick(Event event) {
            this.event = event;
            this.dialogFragment = CalendarDialogFragment.newInstance(event);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            if (dialogFragment.isAdded()) return;
            Props props = AnalyticsHelper.getPropsForEvent(event);
            LiveNationAnalytics.track(AnalyticConstants.CALENDAR_ROW_TAP, AnalyticsCategory.SDP, props);

            dialogFragment.show(getFragmentManager(), "CalendarDialogFragment");
        }
    }
}
