/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.net.Uri;
import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SingleVenueParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.segment.android.models.Props;

import java.util.List;


public class VenueActivity extends DetailBaseFragmentActivity implements EventsView {
    private static final int EVENTS_PER_VENUE_LIMIT = 30;

    public static final String PARAMETER_VENUE_ID = "venue_id";
    private static final String PARAMETER_VENUE_CACHED = "venue_cached";
    private Venue venue;
    private SingleVenueView singleVenueView;
    private EventsView eventsView;
    private Uri appUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_venue);
        if (!getIntent().hasExtra(VenueEventsPresenter.PARAMETER_LIMIT)) {
            getIntent().putExtra(VenueEventsPresenter.PARAMETER_LIMIT, EVENTS_PER_VENUE_LIMIT);
        }
        singleVenueView = (SingleVenueView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
        eventsView = (EventsView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);

        init();

        //Get venue detail
        SingleVenueParameters apiParams = new SingleVenueParameters();
        String venueIdRaw = args.getString(PARAMETER_VENUE_ID);
        long venueId = DataModelHelper.getNumericEntityId(venueIdRaw);
        apiParams.setVenueId(venueId);
        LiveNationApplication.getLiveNationProxy().getSingleVenue(apiParams, new BasicApiCallback<Venue>() {
            @Override
            public void onResponse(Venue venue) {
                VenueActivity.this.venue = venue;
                singleVenueView.setVenue(venue);
                googleViewStart(venue);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                //TODO display an error message
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deinit();
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
        getVenueEventPresenter().initialize(VenueActivity.this, getIntent().getExtras(), VenueActivity.this);
    }

    private void deinit() {
        getVenueEventPresenter().cancel(VenueActivity.this);
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
    protected Props getAnalyticsProps() {
        if (venue != null) {
            Props props = new Props();
            if (args.containsKey(VenueActivity.PARAMETER_VENUE_ID)) {
                String venueIdRaw = args.getString(VenueActivity.PARAMETER_VENUE_ID);
                props.put(AnalyticConstants.VENUE_ID, venueIdRaw);
            }
            return props;
        }
        return null;
    }

    public static Bundle getArguments(String venueIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(VenueActivity.PARAMETER_VENUE_ID, venueIdRaw);
        return bundle;
    }

    public static Bundle getArguments(Long venueIdRaw) {
        Bundle bundle = new Bundle();
        if (venueIdRaw != null) {
            bundle.putString(VenueActivity.PARAMETER_VENUE_ID, venueIdRaw.toString());
        }
        return bundle;
    }

    public static Bundle getArguments(Venue venue) {
        if (venue == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_VENUE_ID, venue.getId().toString());
        bundle.putSerializable(PARAMETER_VENUE_CACHED, venue);
        return bundle;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (appUrl == null && venue != null) {
            googleViewStart(venue);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleViewEnd();
    }

    private void googleViewStart(Venue venue) {
        Uri webUrl = Uri.parse(getString(R.string.web_url_venue) + venue.getId());
        String suffixUrl;
        if (venue.getId().contains("art")) {
            suffixUrl = venue.getId();
        } else {
            suffixUrl = "art_" + venue.getId();
        }
        appUrl = Uri.parse(getString(R.string.app_url_artist) + suffixUrl);

        notifyGoogleViewStart(webUrl, appUrl, venue.getName());

    }

    private void googleViewEnd() {
        notifyGoogleViewEnd(appUrl);
        appUrl = null;
    }
}
