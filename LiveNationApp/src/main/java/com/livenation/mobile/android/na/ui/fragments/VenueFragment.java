/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.VenueShowsActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.EventParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VenueFragment extends LiveNationFragment {

    private static final int EVENTS_PER_VENUE_LIMIT = 30;
    private final static String VENUE = "com.livenation.mobile.android.na.ui.fragments.VenueFragment.VENUE";
    private final static String VENUE_EVENT_LIST = "com.livenation.mobile.android.na.ui.fragments.VenueFragment.EVENT_LIST";

    private ShowsListNonScrollingFragment showsFragment;
    private final static int MAX_INLINE = 3;
    private Venue venue;

    public static VenueFragment newInstance(Venue venue) {
        VenueFragment venueFragment = new VenueFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VENUE, venue);
        venueFragment.setArguments(bundle);
        return venueFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_venue, container, false);

        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        venue = (Venue) getArguments().getSerializable(VENUE);
        List<Event> events = (List<Event>) getArguments().getSerializable(VENUE_EVENT_LIST);
        if (events == null) {
            EventParameters apiParams = new EventParameters();
            apiParams.setPage(0, EVENTS_PER_VENUE_LIMIT);
            LiveNationApplication.getLiveNationProxy().getVenueEvents(venue.getNumericId(), new BasicApiCallback<List<Event>>() {
                @Override
                public void onResponse(List<Event> response) {
                    ArrayList<Event> events = new ArrayList<Event>(response);
                    VenueFragment.this.getArguments().putSerializable(VENUE_EVENT_LIST, events);
                    setEvents(events);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                }
            }, apiParams);

        }

        refresh();
    }


    private void refresh() {
        addFragment(R.id.fragment_venue_header_container, VenueMapFragment.newInstance(venue, true, R.dimen.fragment_venue_map_height), VenueMapFragment.class.getSimpleName());
        addFragment(R.id.fragment_venue_detail_container, VenueDetailFragment.newInstance(venue, true), VenueDetailFragment.class.getSimpleName());

    }

    public void setEvents(ArrayList<Event> events) {
        if (getActivity() != null) {
            showsFragment = ShowsListNonScrollingFragment.newInstance(events, ShowView.DisplayMode.VENUE, AnalyticsCategory.VDP);
            showsFragment.setMaxEvents(MAX_INLINE);
            showsFragment.setDisplayMode(ShowView.DisplayMode.VENUE);
            showsFragment.setMoreShowTitle(R.string.artist_events_overflow);
            showsFragment.setMoreShowClickListener(new ShowAllEventsOnClickListener(venue));

            addFragment(R.id.fragment_venue_container_list, showsFragment, ShowsListNonScrollingFragment.class.getSimpleName());
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
}
