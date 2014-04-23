package com.livenation.mobile.android.na.analytics;

import com.livenation.mobile.android.platform.api.service.livenation.analytics.ErrorTracker;

import java.util.Map;

import io.segment.android.models.Props;

/**
 * Created by elodieferrais on 4/17/14.
 */
public class LibraryErrorTracker implements ErrorTracker {

    @Override
    public void track(String logTitle, Map<String, String> properties) {

        Props props = new Props();
        if (properties != null) {
            for (String key : properties.keySet()) {
                props.put(key, properties.get(key));
            }
        }
        LiveNationAnalytics.track(logTitle, props);

    }
}
