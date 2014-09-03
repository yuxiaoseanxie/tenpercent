/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.OmnitureTracker;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.segment.android.models.Props;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class VenueActivity extends DetailBaseFragmentActivity implements SingleVenueView, EventsView {
    private static final int EVENTS_PER_VENUE_LIMIT = 30;
    private Venue venue;
    private SingleVenueView singleVenueView;
    private EventsView eventsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_venue);
        if (!getIntent().hasExtra(VenueEventsPresenter.PARAMETER_LIMIT)) {
            getIntent().putExtra(VenueEventsPresenter.PARAMETER_LIMIT, EVENTS_PER_VENUE_LIMIT);
        }
        singleVenueView = (SingleVenueView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
        eventsView = (EventsView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
        init();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deinit();
    }

    @Override
    public void setVenue(Venue venue) {
        if (singleVenueView == null) {
            //TODO: this
            throw new RuntimeException("TODO: investigate possible race condition here");
        }
        this.venue = venue;
        singleVenueView.setVenue(venue);
    }

    @Override
    public void setEvents(List<Event> events) {
        if (eventsView == null) {
            //TODO: this
            throw new RuntimeException("TODO: investigate possible race condition here");
        }
        eventsView.setEvents(events);

        invalidateIsShareAvailable();
    }

    private void init() {
        getSingleVenuePresenter().initialize(VenueActivity.this, getIntent().getExtras(), VenueActivity.this);
        getVenueEventPresenter().initialize(VenueActivity.this, getIntent().getExtras(), VenueActivity.this);
    }

    private void deinit() {
        getSingleVenuePresenter().cancel(VenueActivity.this);
        getVenueEventPresenter().cancel(VenueActivity.this);
    }

    private SingleVenuePresenter getSingleVenuePresenter() {
        return LiveNationApplication.get().getSingleVenuePresenter();
    }

    private VenueEventsPresenter getVenueEventPresenter() {
        return LiveNationApplication.get().getVenueEventsPresenter();
    }

    @Override
    protected void onShare() {
        Props props = new Props();
        if (this.venue != null) {
            props.put(AnalyticConstants.VENUE_NAME, venue.getName());
            props.put(AnalyticConstants.VENUE_ID, venue.getId());
        }
        trackActionBarAction(AnalyticConstants.SHARE_ICON_TAP, props);
        super.onShare();
    }

    @Override
    protected void onSearch() {
        trackActionBarAction(AnalyticConstants.SEARCH_ICON_TAP, null);
        super.onSearch();
    }

    //region Share Overrides

    @Override
    protected boolean isShareAvailable() {
        return (venue != null);
    }

    @Override
    protected String getShareSubject() {
        return venue.getName();
    }

    @Override
    protected String getShareText() {
        String venueTemplate = getString(R.string.share_template_venue);
        return venueTemplate.replace("$VENUE", venue.getName())
                .replace("$LINK", venue.getWebUrl());
    }

    //endregion

    private void trackActionBarAction(String event, Props props) {
        if (props == null) {
            props = new Props();
        }
        props.put(AnalyticConstants.SOURCE, AnalyticsCategory.VDP);
        LiveNationAnalytics.track(event, AnalyticsCategory.ACTION_BAR);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_VDP;
    }

    @Override
    protected Map<String, Object> getAnalyticsProps() {
        if (venue != null) {
            Map<String, Object> props = new HashMap<String, Object>();
            if (args.containsKey(SingleVenuePresenter.PARAMETER_VENUE_ID)) {
                String venueIdRaw = args.getString(SingleVenuePresenter.PARAMETER_VENUE_ID);
                props.put(AnalyticConstants.VENUE_ID, venueIdRaw);
            }
            return props;
        }
        return null;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            OmnitureTracker.trackState(AnalyticConstants.OMNITURE_SCREEN_VDP, getAnalyticsProps());
        }
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_VDP;
    }
}
