package com.livenation.mobile.android.na.analytics;

import io.segment.android.Analytics;
import io.segment.android.models.Props;

/**
 * Created by elodieferrais on 4/15/14.
 */
public class LiveNationAnalytics {
    static public void track(String eventTitle, Props props) {
        props.put("Platform", AnalyticConstants.PLATFORM_VALUE);
        Analytics.track(eventTitle + AnalyticConstants.PLATFORM_EVENT_SUFFIX, props);
    }

    static public void track(String eventTitle) {
        Props props = new Props();
        track(eventTitle + AnalyticConstants.PLATFORM_EVENT_SUFFIX, props);
    }

    static public void screen(String screenTitle, Props props) {
        props.put("Platform", AnalyticConstants.PLATFORM_VALUE);
        Analytics.screen(screenTitle + AnalyticConstants.PLATFORM_EVENT_SUFFIX, props);
    }
}
