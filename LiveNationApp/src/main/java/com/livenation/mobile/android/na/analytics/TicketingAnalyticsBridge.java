package com.livenation.mobile.android.na.analytics;

import com.apsalar.sdk.Apsalar;
import com.livenation.mobile.android.ticketing.analytics.AnalyticsHandler;

import java.util.Map;

import android.app.Activity;

public class TicketingAnalyticsBridge implements AnalyticsHandler {
    private static Props mapToProps(Map<String, Object> properties) {
        if (properties == null)
            return null;

        Props props = new Props();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            props.put(entry.getKey(), entry.getValue());
        }
        return props;
    }

    @Override
    public void activityCreated(Activity activity) {
        // Currently not applicable.
        LiveNationAnalytics.logTrace("LifeCycle", "Activity created: " + activity.getClass().getSimpleName());
    }

    @Override
    public void activityStarted(Activity activity) {
        LiveNationAnalytics.logTrace("LifeCycle", "Activity started: " + activity.getClass().getSimpleName());
    }

    @Override
    public void activityPaused(Activity activity) {
        LiveNationAnalytics.logTrace("LifeCycle", "Activity paused: " + activity.getClass().getSimpleName());
    }

    @Override
    public void activityResumed(Activity activity) {
        LiveNationAnalytics.logTrace("LifeCycle", "Activity resumed: " + activity.getClass().getSimpleName());
    }

    @Override
    public void activityStopped(Activity activity) {
        LiveNationAnalytics.logTrace("LifeCycle", "Activity stopped: " + activity.getClass().getSimpleName());
    }

    @Override
    public void track(String event, String category, Map<String, Object> properties) {
        LiveNationAnalytics.track(event, category, mapToProps(properties));
    }

    @Override
    public void screen(String screen, Map<String, Object> properties) {
        LiveNationAnalytics.screen(screen, mapToProps(properties));
    }

    @Override
    public void logError(String error, Map<String, Object> properties) {
        LiveNationAnalytics.track(error, AnalyticsCategory.ERROR, mapToProps(properties));
    }

    @Override
    public void logTrace(String category, String event) {
        LiveNationAnalytics.logTrace(category, event);
    }

    @Override
    public void trackOmnitureState(String pageName, Map<String, Object> properties) {
        OmnitureTracker.trackState(pageName, properties);
    }

    @Override
    public void trackOmnitureAction(String actionName, Map<String, Object> properties) {
        OmnitureTracker.trackAction(actionName, properties);
    }

    @Override
    public void trackApsalarEvent(String actionName, Object... obj) {
        Apsalar.event(actionName, obj);
    }
}
