/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.transport.error.ErrorDictionary;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.util.ArrayList;
import java.util.List;

public class DeviceLocationProvider implements LocationProvider {
    private List<State> activeStates = new ArrayList<State>();

    @Override
    public void getLocation(Context context, ApiService.BasicApiCallback<Double[]> callback) {
        State state = new State(context, callback);
        activeStates.add(state);
        state.run();
    }

    private class State implements LocationListener, Runnable {
        private final android.location.LocationManager locationManager;
        private final Context context;
        private final ApiService.BasicApiCallback<Double[]> callback;

        private State(Context context, ApiService.BasicApiCallback<Double[]> callback) {
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
                callback.onErrorResponse(ErrorDictionary.getUnknownError());
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
            callback.onErrorResponse(ErrorDictionary.getUnknownError());
        }
    }
}
