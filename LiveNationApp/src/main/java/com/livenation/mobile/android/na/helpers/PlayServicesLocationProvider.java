/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.helpers;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.livenation.mobile.android.na.analytics.LibraryErrorTracker;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class PlayServicesLocationProvider implements LocationProvider {


    public static final int STATUS_GOOGLE_PLAY_SUCCESS = 0;
    public static final int STATUS_GOOGLE_PLAY_FAILURE_RECOVERABLE = 1;
    public static final int STATUS_GOOGLE_PLAY_FAILURE_GIVEUP = 2;

    private int getGooglePlayServiceStatus(Context context) {
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
        state.run();
    }

    private class State implements Runnable, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private final GoogleApiClient client;
        private final ProviderCallback<Double[]> callback;
        private final int RETRY_LIMIT = 3;
        private Handler handler;
        private int retryCount;

        private State(ProviderCallback<Double[]> callback, Context context) {
            this.callback = callback;

            this.client = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
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
            //Check to avoid DeadObjectException
            if (client != null && client.isConnected()) {
                try {

                    Location location = LocationServices.FusedLocationApi.getLastLocation(client);

                    if (null != location) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        Double[] locationArray = new Double[]{lat, lng};
                        callback.onResponse(locationArray);
                        client.disconnect();
                    } else {
                        client.disconnect();
                        retry();
                    }
                } catch (IllegalStateException ex) {
                    new LibraryErrorTracker().track("PlayServicesLocationProvider:IllegalStateException:" + ex.toString(), null);
                    callback.onErrorResponse();
                }


            } else {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("Client", client);
                if (client != null) {
                    data.put("isConnected", client.isConnected());
                }
                new LibraryErrorTracker().track("PlayServicesLocationProvider:DeadObjectException", data);
                callback.onErrorResponse();
            }

        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        private void retry() {
            handler = new Handler();
            if (retryCount < RETRY_LIMIT) {
                handler.postDelayed(this, 1000);
                retryCount++;
            } else {
                callback.onErrorResponse();
            }
        }

    }

}
