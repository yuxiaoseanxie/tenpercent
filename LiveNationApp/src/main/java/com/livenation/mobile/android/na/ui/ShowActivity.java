/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SingleEventParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.segment.android.models.Props;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class ShowActivity extends DetailBaseFragmentActivity {
    private static SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("MMM d", Locale.US);
    public static final String PARAMETER_EVENT_ID = "event_id";
    public static final String PARAMETER_EVENT_CACHED = "event_cached";

    private GoogleApiClient googleApiClient;
    private Event event;
    private Uri appUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_show);

        SingleEventParameters apiParams = new SingleEventParameters();
        if (args.containsKey(PARAMETER_EVENT_ID)) {
            String eventIdRaw = args.getString(PARAMETER_EVENT_ID);
            long eventId = DataModelHelper.getNumericEntityId(eventIdRaw);
            apiParams.setEventId(eventId);
        }

        googleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.APP_INDEX_API).build();
        googleApiClient.connect();

        //Use cached event for avoiding the blank page while we are waiting for the http response
        if (args.containsKey(PARAMETER_EVENT_CACHED)) {
            Event event = (Event) args.getSerializable(PARAMETER_EVENT_CACHED);
            setEvent(event);
        } else {


            LiveNationApplication.getLiveNationProxy().getSingleEvent(apiParams, new BasicApiCallback<Event>() {
                @Override
                public void onResponse(Event event) {
                    setEvent(event);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    //TODO display an error message
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateUp() {
        Intent intent = new Intent(ShowActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onShare() {
        Props props = new Props();
        if (this.event != null) {
            props.put(AnalyticConstants.EVENT_NAME, event.getName());
            props.put(AnalyticConstants.EVENT_ID, event.getId());
        }
        trackActionBarAction(AnalyticConstants.SHARE_ICON_TAP, props);
        super.onShare();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (appUrl == null && event != null) {
            googleApiClient.connect();
            googleViewStart(event);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleViewEnd();
        googleApiClient.disconnect();
    }

    @Override
    protected void onSearch() {
        trackActionBarAction(AnalyticConstants.SEARCH_ICON_TAP, null);
        super.onSearch();
    }

    //region Share Overrides

    @Override
    protected boolean isShareAvailable() {
        return (event != null);
    }

    @Override
    protected String getShareSubject() {
        return event.getName();
    }

    @Override
    protected String getShareText() {
        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        SHORT_DATE_FORMATTER.setTimeZone(timeZone);

        String eventTemplate = getString(R.string.share_template_show);
        return eventTemplate.replace("$HEADLINE_ARTIST", event.getDisplayName())
                .replace("$SHORT_DATE", SHORT_DATE_FORMATTER.format(event.getLocalStartTime()))
                .replace("$VENUE", event.getVenue().getName())
                .replace("$LINK", event.getWebUrl());
    }

    //endregion

    private void trackActionBarAction(String event, Props props) {
        if (props == null) {
            props = new Props();
        }
        props.put(AnalyticConstants.SOURCE, AnalyticsCategory.SDP);
        LiveNationAnalytics.track(event, AnalyticsCategory.ACTION_BAR);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_SDP;
    }

    @Override
    protected Props getAnalyticsProps() {
        if (event != null) {
            Props props = new Props();
            props.put(AnalyticConstants.EVENT_ID, event.getId());

            if (event.getVenue() != null) {
                props.put(AnalyticConstants.VENUE_ID, event.getVenue().getId());
            }
            if (event.getLineup() != null && event.getLineup().size() > 0) {
                props.put(AnalyticConstants.ARTIST_ID, event.getLineup().get(0).getId());
            }

            return props;
        }
        return null;
    }

    private void setEvent(Event event) {
        final SingleEventView singleEventView = (SingleEventView) getSupportFragmentManager().findFragmentById(R.id.activity_show_content);

        ShowActivity.this.event = event;
        singleEventView.setEvent(event);
        invalidateIsShareAvailable();
        googleViewStart(event);
    }

    public static Bundle getArguments(String eventIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_EVENT_ID, eventIdRaw);
        return bundle;
    }

    public static Bundle getArguments(Event event) {
        if (event == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_EVENT_ID, event.getId());
        if (null != event) {
            bundle.putSerializable(PARAMETER_EVENT_CACHED, event);
        }
        return bundle;


    }

    private void googleViewStart(Event event) {
        Uri webUrl = Uri.parse(getString(R.string.web_url_show) + DataModelHelper.getNumericEntityId(event.getId()));
        String suffixUrl;
        if (event.getId().contains("evt")) {
            suffixUrl = event.getId();
        } else {
            suffixUrl = "evt_" + event.getId();
        }
        appUrl = Uri.parse(getString(R.string.app_url_show) + suffixUrl);

        notifyGoogleViewStart(googleApiClient, webUrl, appUrl, event.getName());

    }

    private void googleViewEnd() {
        notifyGoogleViewEnd(googleApiClient, appUrl);
        appUrl = null;
    }
}
