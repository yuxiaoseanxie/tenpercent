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
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.util.List;


public class VenueActivity extends DetailBaseFragmentActivity implements SingleVenueView, EventsView {
    private static final int EVENTS_PER_VENUE_LIMIT = 3;
    private Venue venue;
    private SingleVenueView singleVenueView;
    private EventsView eventsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);
        if (!getIntent().hasExtra(VenueEventsPresenter.PARAMETER_LIMIT)) {
            getIntent().putExtra(VenueEventsPresenter.PARAMETER_LIMIT, EVENTS_PER_VENUE_LIMIT);
        }

        singleVenueView = (SingleVenueView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
        eventsView = (EventsView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
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


    //region Share Overrides

    @Override
    protected boolean isShareAvailable() {
        return (venue != null);
    }

    @Override
    protected String getShareTitle() {
        return "Venue";
    }

    @Override
    protected String getShareText() {
        return "Check out " + venue.getName();
    }

    //endregion
}
