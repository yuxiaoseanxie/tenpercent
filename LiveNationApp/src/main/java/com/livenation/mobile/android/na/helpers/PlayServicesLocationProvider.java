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
import com.livenation.mobile.android.na.helpers.LocationHelper.LocationCallback;
import com.livenation.mobile.android.platform.util.Logger;

public class PlayServicesLocationProvider implements
		LocationHelper.LocationProvider,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener  {
	
	private Handler handler;
	private int retryCount;
	private LocationCallback callback;
	private LocationClient client;
	private final int RETRY_LIMIT = 3;

	public static final int STATUS_GOOGLE_PLAY_SUCCESS = 0;
	public static final int STATUS_GOOGLE_PLAY_FAILURE_RECOVERABLE = 1;
	public static final int STATUS_GOOGLE_PLAY_FAILURE_GIVEUP = 2;
	
	@Override
	public void getLocation(Context context, LocationCallback callback) {
		this.callback = callback;
		client = new LocationClient(context, this, this);
		client.connect();
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Logger.log("PlayServicesLocation", "Error binding to LocationClient: " + result.getErrorCode());
		callback.onLocationFailure(LocationHelper.FAILURE_UNKNOWN);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		payload.run();
	}

	@Override
	public void onDisconnected() {
	}
	
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

	private final Runnable payload = new Runnable() {
		public void run() {
			Location location = client.getLastLocation();
			if (null != location) {
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				callback.onLocation(lat, lng);
			} else {
				handler = new Handler();
				if (retryCount < RETRY_LIMIT) {
					Logger.log("Location", "Location was null, retrying.. (" + retryCount + ")");
					handler.postDelayed(this, 1000);
					retryCount++;
				} else {
					callback.onLocationFailure(LocationHelper.FAILURE_UNKNOWN);
				}
			}
		}
	};
}
