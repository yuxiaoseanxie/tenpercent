/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.providers.location;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class DeviceLocationProvider implements LocationProvider {
    private List<State> activeStates = new ArrayList<State>();

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        Double[] array = {34.0878, -118.3722};
        callback.onResponse(array);
        //State state = new State(LiveNationApplication.get().getApplicationContext(), callback);
        //activeStates.add(state);
        //state.run();
    }

    private class State implements LocationListener, Runnable {
        private final android.location.LocationManager locationManager;
        private final Context context;
        private final ProviderCallback<Double[]> callback;

        private State(Context context, ProviderCallback<Double[]> callback) {
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
                callback.onErrorResponse();
                return;
            }
            Location last = locationManager.getLastKnownLocation(provider);
            if (null != last) {
                callback.onResponse(new Double[]{last.getLatitude(), last.getLongitude()});
            } else {
                locationManager.requestLocationUpdates(provider, 1000, 0, this);
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(this);
            callback.onResponse(new Double[]{location.getLatitude(), location.getLongitude()});
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
            callback.onErrorResponse();
        }
    }
}
