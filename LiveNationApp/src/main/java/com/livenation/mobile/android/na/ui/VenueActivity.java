/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.util.List;


public class VenueActivity extends LiveNationFragmentActivity implements SingleVenueView, EventsView {
    private SingleVenueView singleVenueView;
    private EventsView eventsView;
    private static final int EVENTS_PER_VENUE_LIMIT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);
        if (!getIntent().hasExtra(VenueEventsPresenter.PARAMETER_LIMIT)) {
            getIntent().putExtra(VenueEventsPresenter.PARAMETER_LIMIT, EVENTS_PER_VENUE_LIMIT);
        }
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
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (null == singleVenueView) {
            singleVenueView = (SingleVenueView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
        }
        if (null == eventsView) {
            eventsView = (EventsView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
        }
    }

    @Override
    public void setVenue(Venue venue) {
        if (singleVenueView == null) {
            //TODO: this
            throw new RuntimeException("TODO: investigate possible race condition here");
        }
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

}
