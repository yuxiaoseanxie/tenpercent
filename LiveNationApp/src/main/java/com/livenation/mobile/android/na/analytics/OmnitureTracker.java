package com.livenation.mobile.android.na.analytics;

import com.adobe.mobile.Analytics;

import java.util.Map;

/**
 * Created by elodieferrais on 8/20/14.
 */
public class OmnitureTracker {
    public static void trackAction(String state, Map<String, Object> contextData) {
        Analytics.trackAction(state, contextData);
    }
}
