/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.helpers;

import android.content.Context;

import com.livenation.mobile.android.platform.util.Logger;

public class LocationHelper {
	public static final int FAILURE_UNKNOWN = 0;
	public static final int FAILURE_GOOGLE_SERVICES_USER_REQUIRED = 1;
	
	private LocationCache locationCache = null;
	private LocationProvider currentRequest = null;
	
	public void prepareCache(Context context) {
		LocationCallback callback = new LocationCallback() {
			@Override
			public void onLocation(double lat, double lng) {
				Logger.log("LocationHelper", "Location cached: " + lat + ", " + lng);
				setLocationCache(lat, lng);
			}

			@Override
			public void onLocationFailure(int failureCode) {
				//do nothing, just trying a sneaky cache
			}
		};
		
		getLocation(context, callback);
	}

	public void getLocation(Context context, LocationCallback callback) {
		if (hasLocationCache()) {
			Logger.log("LocationHelper", "Location returned from cache: " + locationCache.getLatitude() + ", " + locationCache.getLongitude());
			completeLocationRequest(locationCache.getLatitude(),
					locationCache.getLongitude(), callback);
		} else {
			LocationCallback wrapped = getCacheableCallback(callback);
			
			switch (PlayServicesLocationProvider.getGooglePlayServiceStatus(context)) {
			case PlayServicesLocationProvider.STATUS_GOOGLE_PLAY_SUCCESS :
				currentRequest = new PlayServicesLocationProvider();
				currentRequest.getLocation(context, wrapped);
				break;
			case PlayServicesLocationProvider.STATUS_GOOGLE_PLAY_FAILURE_RECOVERABLE:
				//TODO: Handle Google Services recoverable intent;
				currentRequest = new DummyLocationProvider();
				currentRequest.getLocation(context, wrapped);
				//callback.onLocationFailure(FAILURE_GOOGLE_SERVICES_USER_REQUIRED);
				break;
			case PlayServicesLocationProvider.STATUS_GOOGLE_PLAY_FAILURE_GIVEUP:
				//TODO: Fallback to GPS/Network location here: DeviceLocationProvider();
				currentRequest = new DummyLocationProvider();
				currentRequest.getLocation(context, wrapped);
				break;	
			}
		}
	}

	private void setLocationCache(double lat, double lng) {
		locationCache = new LocationCache(lat, lng);
	}

	public void clearLocationCache() {
		locationCache = null;
	}

	private boolean hasLocationCache() {
		return locationCache != null;
	}

	private void completeLocationRequest(double lat, double lng,
			LocationCallback callback) {
		callback.onLocation(lat, lng);
	}

	private class LocationCache {
		private final double lat;
		private final double lng;

		public LocationCache(double lat, double lng) {
			this.lat = lat;
			this.lng = lng;
		}

		public double getLatitude() {
			return lat;
		}

		public double getLongitude() {
			return lng;
		}
	}

	private LocationCallback getCacheableCallback(final LocationCallback callback) {
		LocationCallback wrapper = new LocationCallback() {
			
			@Override
			public void onLocationFailure(int failureCode) {
				callback.onLocationFailure(failureCode);
			}
			
			@Override
			public void onLocation(double lat, double lng) {
				setLocationCache(lat, lng);
				callback.onLocation(lat, lng);
			}
		};
		return wrapper;
	}
	
	public static interface LocationCallback {
		void onLocation(double lat, double lng);
		void onLocationFailure(int failureCode);
	}
	
	static interface LocationProvider {
		void getLocation(Context context, LocationCallback callback);
	}
}
