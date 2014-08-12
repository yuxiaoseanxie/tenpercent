/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.util.ArrayList;
import java.util.List;

public class PlayServicesLocationProvider implements LocationProvider {


    public static final int STATUS_GOOGLE_PLAY_SUCCESS = 0;
    public static final int STATUS_GOOGLE_PLAY_FAILURE_RECOVERABLE = 1;
    public static final int STATUS_GOOGLE_PLAY_FAILURE_GIVEUP = 2;

    private List<State> activeStates = new ArrayList<State>();

    public static int getGooglePlayServiceStatus(Context context) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if (status == ConnectionResult.SUCCESS) {
            return STATUS_GOOGLE_PLAY_SUCCESS;
        }
        if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
            return STATUS_GOOGLE_PLAY_FAILURE_RECOVERABLE;
        }
        return STATUS_GOOGLE_PLAY_FAILURE_GIVEUP;
    }

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        if (STATUS_GOOGLE_PLAY_SUCCESS != getGooglePlayServiceStatus(LiveNationApplication.get().getApplicationContext())) {
            callback.onErrorResponse();
            return;
        }
        State state = new State(callback, LiveNationApplication.get().getApplicationContext());
        //stop it from getting GC'd
        activeStates.add(state);
        state.run();
    }

    private class State implements GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener, Runnable {
        private final LocationClient client;
        private final ProviderCallback<Double[]> callback;
        private final int RETRY_LIMIT = 3;
        private Handler handler;
        private int retryCount;

        private State(ProviderCallback<Double[]> callback, Context context) {
            this.callback = callback;
            this.client = new LocationClient(context, this, this);
        }

        @Override
        public void run() {
            client.connect();
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e("PlayServicesLocation", "Error binding to LocationClient: " + result.getErrorCode());
            callback.onErrorResponse();
        }

        @Override
        public void onConnected(Bundle connectionHint) {
            Location location = client.getLastLocation();
            if (null != location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                Double[] locationArray = new Double[]{lat, lng};
                callback.onResponse(locationArray);
                cleanUp();
            } else {
                handler = new Handler();
                if (retryCount < RETRY_LIMIT) {
                    handler.postDelayed(this, 1000);
                    retryCount++;
                } else {
                    callback.onErrorResponse();
                    cleanUp();
                }
            }
            client.disconnect();
        }

        @Override
        public void onDisconnected() {
        }

        private void cleanUp() {
            activeStates.remove(this);
        }

    }

}
