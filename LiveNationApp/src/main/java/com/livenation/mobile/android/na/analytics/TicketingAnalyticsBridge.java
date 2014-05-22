package com.livenation.mobile.android.na.analytics;

import android.app.Activity;

import com.livenation.mobile.android.ticketing.analytics.AnalyticsHandler;

import java.util.Map;

import io.segment.android.Analytics;
import io.segment.android.models.Props;

public class TicketingAnalyticsBridge implements AnalyticsHandler {
    @Override
    public void activityCreated(Activity activity) {
        // Currently not applicable.
    }

    @Override
    public void activityStarted(Activity activity) {
        Analytics.activityStart(activity);
    }

    @Override
    public void activityPaused(Activity activity) {
        Analytics.activityPause(activity);
    }

    @Override
    public void activityResumed(Activity activity) {
        Analytics.activityResume(activity);
    }

    @Override
    public void activityStopped(Activity activity) {
        Analytics.activityStop(activity);
    }


    private static Props mapToProps(Map<String, String> properties) {
        if (properties == null)
            return null;

        Props props = new Props();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            props.put(entry.getKey(), entry.getValue());
        }
        return props;
    }

    @Override
    public void track(String event, Map<String, String> properties) {
        LiveNationAnalytics.track(event, mapToProps(properties));
    }

    @Override
    public void screen(String screen, Map<String, String> properties) {
        LiveNationAnalytics.screen(screen, mapToProps(properties));
    }
}
