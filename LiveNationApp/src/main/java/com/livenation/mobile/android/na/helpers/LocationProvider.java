package com.livenation.mobile.android.na.helpers;

import android.content.Context;

/**
 * Created by cchilton on 3/13/14.
 */
public interface LocationProvider {
    void getLocation(Context context, LocationCallback callback);

    public static interface LocationCallback {
        void onLocation(double lat, double lng);
        void onLocationFailure(int failureCode);
    }
}
