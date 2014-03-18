/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.location.*;
import android.os.Bundle;

import com.livenation.mobile.android.platform.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class DeviceLocationProvider implements LocationProvider {
    private List<State> activeStates = new ArrayList<State>();
    @Override
    public void getLocation(Context context, LocationCallback callback) {
        State state = new State(context, callback);
        activeStates.add(state);
        state.run();
    }

    private class State implements LocationListener, Runnable {
        private final android.location.LocationManager locationManager;
        private final Context context;
        private final LocationCallback callback;

        private State(Context context, LocationCallback callback) {
            this.context = context;
            this.callback = callback;
            locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        @Override
        public void run() {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, false);
            if (null == provider) {
                //no location providers, may be an emulator
                callback.onLocationFailure(0);
                return;
            }
            Location last = locationManager.getLastKnownLocation(provider);
            if (null != last) {
                callback.onLocation(last.getLatitude(), last.getLongitude());
            } else {
                locationManager.requestLocationUpdates(provider, 1000, 0, this);
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(this);
            callback.onLocation(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {
            callback.onLocationFailure(0);
        }
    }
}
