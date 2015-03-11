package com.livenation.mobile.android.na.analytics.services;

import com.crashlytics.android.Crashlytics;

import android.mobile.livenation.com.livenationui.analytics.CrashService;
import android.util.Log;

/**
 * Created by elodieferrais on 3/10/15.
 */
public class CrashlyticsAnalytics implements CrashService {
    @Override
    public void logTrace(String tag, String data) {
        Crashlytics.log(Log.VERBOSE, tag, data);
    }
}
