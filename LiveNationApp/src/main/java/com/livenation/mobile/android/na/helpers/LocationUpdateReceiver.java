package com.livenation.mobile.android.na.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.livenation.mobile.android.na.providers.location.DummyLocationProvider;
import com.livenation.mobile.android.na.providers.location.LocationManager;

/**
 * Created by elodieferrais on 4/24/14.
 */
public class LocationUpdateReceiver extends BroadcastReceiver{

    public static final String EXTRA_MODE_KEY = "mode";
    public static final String EXTRA_LNG_KEY = "longitude";
    public static final String EXTRA_LAT_KEY = "latitude";
    private LocationUpdateListener listener;

    public LocationUpdateReceiver(LocationUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int mode = intent.getIntExtra(EXTRA_MODE_KEY, LocationManager.MODE_SYSTEM);
        double lat = intent.getDoubleExtra(EXTRA_LAT_KEY, DummyLocationProvider.LOCATION_SF[0]);
        double lng = intent.getDoubleExtra(EXTRA_LNG_KEY, DummyLocationProvider.LOCATION_SF[1]);

        listener.onLocationUpdated(mode, lat, lng);

    }

    public static interface LocationUpdateListener {
        public void onLocationUpdated(int mode, double lat, double lng);
    };
}
