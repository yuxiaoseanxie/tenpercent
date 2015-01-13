package com.livenation.mobile.android.na.analytics.services;

import com.livenation.mobile.android.na.analytics.Props;

/**
 * Created by cchilton on 1/12/15.
 */
public interface AnalyticService {
    void screen(String screenTitle, Props props);

    void track(String event, Props props);
}
