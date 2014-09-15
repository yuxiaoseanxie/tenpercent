package com.livenation.mobile.android.na.analytics;

import com.livenation.mobile.android.platform.api.service.livenation.analytics.ErrorTracker;
import com.segment.android.models.Props;

import java.util.Map;


/**
 * Created by elodieferrais on 4/17/14.
 */
public class LibraryErrorTracker implements ErrorTracker {

    @Override
    public void track(String logTitle, Map<String, Object> properties) {

        Props props = new Props();
        if (properties != null) {
            for (String key : properties.keySet()) {
                props.put(key, properties.get(key));
            }
        }
        LiveNationAnalytics.track(logTitle, AnalyticsCategory.ERROR, props);

    }
}
