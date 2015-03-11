package com.livenation.mobile.android.na.analytics.services;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.livenation.mobile.android.na.R;

import android.content.Context;
import android.mobile.livenation.com.livenationui.analytics.AnalyticService;
import android.mobile.livenation.com.livenationui.analytics.Props;

/**
 * Created by cchilton on 1/12/15.
 */
public class GoogleAnalytics implements AnalyticService {
    private static Tracker googleAnalytics;
    private static String GOOGLE_ANALYTICS_DEFAULT_CATEGORY = "All";

    public GoogleAnalytics(Context context) {
        googleAnalytics = com.google.android.gms.analytics.GoogleAnalytics.getInstance(context).newTracker(R.xml.google_analytics);
    }

    @Override
    public void screen(String screenTitle, Props props) {
        googleAnalytics.setScreenName(screenTitle);
        googleAnalytics.send(new HitBuilders.AppViewBuilder().build());
        googleAnalytics.setScreenName(null);
    }

    @Override
    public void track(String event, Props props) {
        for (String key : props.keySet()) {
            googleAnalytics.set(key, String.valueOf(props.get(event)));
        }

        googleAnalytics.send(new HitBuilders.EventBuilder().setCategory(GOOGLE_ANALYTICS_DEFAULT_CATEGORY)
                .setAction(event)
                .build());
    }
}
