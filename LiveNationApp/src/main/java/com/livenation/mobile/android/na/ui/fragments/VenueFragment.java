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
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.VenueShowsActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.OverflowView;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VenueFragment extends LiveNationFragment implements SingleVenueView, EventsView {
    private final String SHOWS_FRAGMENT_TAG = "shows";
    private final String MAP_FRAGMENT_TAG = "maps";

    private ShowsListNonScrollingFragment showsFragment;
    private final static int MAX_INLINE = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_venue, container, false);

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

        showsFragment.setMaxEvents(MAX_INLINE);
        showsFragment.setDisplayMode(ShowView.DisplayMode.VENUE);
        OverflowView showMoreView = new OverflowView(getActivity());
        showMoreView.setTitle(R.string.artist_events_overflow);

        showsFragment.setShowMoreItemsView(showMoreView);
    }


    @Override
    public void setVenue(Venue venue) {
        showsFragment.getShowMoreItemsView().setOnClickListener(new ShowAllEventsOnClickListener(venue));
        getFragmentManager().beginTransaction().add(R.id.fragment_venue_header_container, VenueMapFragment.newInstance(venue)).commit();
        getFragmentManager().beginTransaction().add(R.id.fragment_venue_detail_container, VenueDetailFragment.newInstance(venue, true)).commit();

    }

    @Override
    public void setEvents(List<Event> events) {
        showsFragment.setEvents(events);
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
