package com.livenation.mobile.android.na.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

/**
 * Created by elodieferrais on 4/24/14.
 */
public class LocationUpdateReceiver extends BroadcastReceiver {

    public static final String EXTRA_MODE_KEY = "mode";
    public static final String EXTRA_CITY = "city";
    private LocationUpdateListener listener;

    public LocationUpdateReceiver(LocationUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int mode = intent.getIntExtra(EXTRA_MODE_KEY, LocationManager.MODE_SYSTEM);
        City city = (City) intent.getSerializableExtra(EXTRA_CITY);
        if (TextUtils.isEmpty(city.getName())) {
            city.setName(LocationManager.UNKNOWN_LOCATION);
        }

        listener.onLocationUpdated(mode, city);

    }

    public static interface LocationUpdateListener {
        public void onLocationUpdated(int mode, City city);
    }

    ;
}
