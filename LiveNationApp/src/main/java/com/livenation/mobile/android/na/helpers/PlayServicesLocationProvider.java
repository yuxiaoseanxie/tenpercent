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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.transport.error.ErrorDictionary;
import com.livenation.mobile.android.platform.util.Logger;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.util.ArrayList;
import java.util.List;

public class PlayServicesLocationProvider implements
        LocationProvider {


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
    public void getLocation(Context context, ApiService.BasicApiCallback<Double[]> callback) {
        if (STATUS_GOOGLE_PLAY_SUCCESS != getGooglePlayServiceStatus(context)) {
            callback.onErrorResponse(ErrorDictionary.getUnknownError());
            return;
        }
        State state = new State(callback, context);
        //stop it from getting GC'd
        activeStates.add(state);
        state.run();
    }

    private class State implements GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener, Runnable {
        private final LocationClient client;
        private final ApiService.BasicApiCallback<Double[]> callback;
        private final int RETRY_LIMIT = 3;
        private Handler handler;
        private int retryCount;

        private State(ApiService.BasicApiCallback<Double[]> callback, Context context) {
            this.callback = callback;
            this.client = new LocationClient(context, this, this);
        }

        @Override
        public void run() {
            client.connect();
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Logger.log("PlayServicesLocation", "Error binding to LocationClient: " + result.getErrorCode());
            callback.onErrorResponse(ErrorDictionary.getUnknownError());
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
                    Logger.log("Location", "Location was null, retrying.. (" + retryCount + ")");
                    handler.postDelayed(this, 1000);
                    retryCount++;
                } else {
                    callback.onErrorResponse(ErrorDictionary.getUnknownError());
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
