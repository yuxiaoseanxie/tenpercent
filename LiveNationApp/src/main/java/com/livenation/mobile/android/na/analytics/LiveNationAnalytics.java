package com.livenation.mobile.android.na.analytics;


import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.livenation.mobile.android.na.analytics.services.AmplitudeAnalytics;
import com.livenation.mobile.android.na.analytics.services.AnalyticService;
import com.livenation.mobile.android.na.analytics.services.GoogleAnalytics;
import com.livenation.mobile.android.na.helpers.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/15/14.
 */
public class LiveNationAnalytics {
    private static final List<AnalyticService> analyticServices = new ArrayList<>();

    static public void initialize(Context context) {
        analyticServices.clear();
        analyticServices.add(new GoogleAnalytics(context));
        analyticServices.add(new AmplitudeAnalytics(context));
    }

    @VisibleForTesting
    static public void initialize(AnalyticService... analytics) {
        analyticServices.clear();
        for (int i = 0; i < analytics.length; i++) {
            analyticServices.add(analytics[i]);
        }
    }

    @VisibleForTesting
    static public List<AnalyticService> getAnalyticServices() {
        return analyticServices;
    }

    static public void track(String eventTitle, AnalyticsCategory category, Props props) {
        if (category == null) {
            category = AnalyticsCategory.UNKNOWN;
        }
        track(eventTitle, category.categoryName, props);
    }

    static public void track(String eventTitle, AnalyticsCategory category) {
        track(eventTitle, category, null);
    }

    static public void track(String eventTitle, String category, Props props) {
        if (props == null) {
            props = new Props();
        }

        if (category == null) {
            category = AnalyticsCategory.UNKNOWN.categoryName;
        }

        props.put(AnalyticConstants.CATEGORY, category);
        props.put(AnalyticConstants.PLATFORM, AnalyticConstants.PLATFORM_VALUE);

        for (AnalyticService service : analyticServices) {
            service.track(eventTitle + AnalyticConstants.PLATFORM_EVENT_SUFFIX, props);
        }

        logTrace(eventTitle, category + ": " + props.toString());
    }

    static public void screen(String screenTitle, Props props) {
        if (props == null)
            props = new Props();


        props.put(AnalyticConstants.PLATFORM, AnalyticConstants.PLATFORM_VALUE);

        for (AnalyticService service : analyticServices) {
            service.screen(screenTitle + AnalyticConstants.PLATFORM_EVENT_SUFFIX, props);
        }

        logTrace("Screen", screenTitle + ": " + props.toString());
    }

    static public void logTrace(String tag, String data) {
        Crashlytics.log(Log.VERBOSE, tag, data);
    }
}
