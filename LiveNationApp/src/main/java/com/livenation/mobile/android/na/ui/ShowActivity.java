/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import io.segment.android.models.Props;

public class ShowActivity extends DetailBaseFragmentActivity implements SingleEventView {
    private static SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("MMM d", Locale.US);

    private Event event;
    private SingleEventView singleEventView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_show);

        singleEventView = (SingleEventView) getSupportFragmentManager().findFragmentById(R.id.activity_show_content);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deinit();
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

    @Override
    public void setEvent(Event event) {
        if (singleEventView == null) {
            //TODO: Possible race condition?
            return;
        }
        this.event = event;
        singleEventView.setEvent(event);

        invalidateIsShareAvailable();
    }

    private void init() {
        getSingleEventPresenter().initialize(ShowActivity.this, getIntent().getExtras(), ShowActivity.this);
    }

    private void deinit() {
        getSingleEventPresenter().cancel(ShowActivity.this);
    }

    private SingleEventPresenter getSingleEventPresenter() {
        return LiveNationApplication.get().getSingleEventPresenter();
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
        TimeZone timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
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
            if (args.containsKey(SingleEventPresenter.PARAMETER_EVENT_ID)) {
                String eventIdRaw = args.getString(SingleEventPresenter.PARAMETER_EVENT_ID);
                props.put(AnalyticConstants.EVENT_ID, eventIdRaw);
            }
            return props;
        }
        return null;
    }
}
