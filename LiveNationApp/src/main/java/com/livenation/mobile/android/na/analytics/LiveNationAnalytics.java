package com.livenation.mobile.android.na.analytics;


import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.segment.android.Analytics;
import com.segment.android.models.Props;

/**
 * Created by elodieferrais on 4/15/14.
 */
public class LiveNationAnalytics {


    static public void track(String eventTitle, AnalyticsCategory category, Props props) {
        if (category == null) {
            category = AnalyticsCategory.UNKNOWN;
        }
        track(eventTitle, category.categoryName, props);
    }

    static public void track(String eventTitle, String category, Props props) {
        if (props == null) {
            props = new Props();
        }

        if (category == null) {
            category = AnalyticsCategory.UNKNOWN.categoryName;
        }
        props.put(AnalyticConstants.CATEGORY, category);
        props.put("Platform", AnalyticConstants.PLATFORM_VALUE);
        Analytics.track(eventTitle + AnalyticConstants.PLATFORM_EVENT_SUFFIX, props);
        traceLog(eventTitle, category, props.toString());
    }

    static public void track(String eventTitle, AnalyticsCategory category) {

        track(eventTitle, category, null);
    }

    static public void screen(String screenTitle, Props props) {
        if (props == null)
            props = new Props();

        props.put("Platform", AnalyticConstants.PLATFORM_VALUE);
        Analytics.screen(screenTitle + AnalyticConstants.PLATFORM_EVENT_SUFFIX, props);
        traceLog(screenTitle, "Screen", props.toString());
    }

    static private void traceLog(String eventTitle, String category, String data) {
        String title = String.format("%s: %s", eventTitle, category);
        Crashlytics.log(Log.VERBOSE, title, data);
    }
}
