/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments.support;

import android.support.v4.app.Fragment;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

public abstract class LiveNationFragment extends Fragment implements LiveNationFragmentContract {
	
	@Override
	public LiveNationApiService getApiService() {
		return LiveNationApplication.get().getServiceApi();
	}
	
	@Override
	public LocationHelper getLocationHelper() {
		return LiveNationApplication.get().getLocationHelper();
	}
	
	@Override
	public ImageLoader getImageLoader() {
		return LiveNationApplication.get().getImageLoader();
	}
}
