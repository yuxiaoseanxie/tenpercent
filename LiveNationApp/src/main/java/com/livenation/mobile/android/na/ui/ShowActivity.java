/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.content.Intent;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.analytics.Props;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

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

        // Start google app indexing
        googleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.APP_INDEX_API).build();
        googleApiClient.connect();


        Long eventId = null;
        if (args.containsKey(PARAMETER_EVENT_ID)) {
            String eventIdRaw = args.getString(PARAMETER_EVENT_ID);
            eventId = DataModelHelper.getNumericEntityId(eventIdRaw);
        }


        //Use cached event for avoiding the blank page while we are waiting for the http response
        if (args.containsKey(PARAMETER_EVENT_CACHED)) {
            event = (Event) args.getSerializable(PARAMETER_EVENT_CACHED);
            setEvent(event);
        } else if (eventId != null) {
            LiveNationApplication.getLiveNationProxy().getSingleEvent(eventId, new BasicApiCallback<Event>() {
                @Override
                public void onResponse(Event event) {
                    setEvent(event);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    Toast.makeText(getApplicationContext(), R.string.internet_broken, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            finish();
            return;
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
    protected Map<String, Object> getAnalyticsProps() {
        Map<String, Object> props = new HashMap<String, Object>();

        if (args.containsKey(PARAMETER_EVENT_ID)) {
            props.put(AnalyticConstants.EVENT_ID, DataModelHelper.getNumericEntityId(args.getString(PARAMETER_EVENT_ID)));
        }
        if (event != null) {
            props.put(AnalyticConstants.EVENT_ID, event.getNumericId());

            if (event.getVenue() != null) {
                props.put(AnalyticConstants.VENUE_ID, event.getVenue().getNumericId());
            }
            if (event.getLineup() != null && event.getLineup().size() > 0) {
                props.put(AnalyticConstants.ARTIST_ID, event.getLineup().get(0).getNumericId());
            }
        }
        return props;
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_SDP;
    }

    private void setEvent(Event event) {
        final SingleEventView singleEventView = (SingleEventView) getSupportFragmentManager().findFragmentById(R.id.activity_show_content);
        if (singleEventView == null) {
            //user exited the activity before the network response came back
            return;
        }
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

    @Override
    protected Map<String, Object> getOmnitureProductsProps() {
        if (args.containsKey(PARAMETER_EVENT_ID)) {
            HashMap cdata = new HashMap<String, Object>();
            cdata.put("&&products", ";" + DataModelHelper.getNumericEntityId(args.getString(PARAMETER_EVENT_ID)));
            return cdata;
        }
        return null;
    }
}
