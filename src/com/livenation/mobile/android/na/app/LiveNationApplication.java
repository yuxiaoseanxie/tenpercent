/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

import android.app.Application;

import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.LiveNationApiServiceImpl;

public class LiveNationApplication extends Application {
	private static LiveNationApplication instance;
	private static LiveNationApiService serviceApi;
	private static LocationHelper locationHelper;
	
	public static LiveNationApplication get() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		serviceApi = new LiveNationApiServiceImpl(Constants.clientId, Constants.deviceId, getApplicationContext());
		locationHelper = new LocationHelper();
		locationHelper.prepareCache(getApplicationContext());
	}
	
	public LiveNationApiService getServiceApi() {
		return serviceApi;
	}
	
	public LocationHelper getLocationHelper() {
		return locationHelper;
	}
}
