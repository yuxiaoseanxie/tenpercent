/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
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
	
	@Override
	public EventsPresenter getEventsPresenter() {
		return LiveNationApplication.get().getEventsPresenter();
	}
	
	@Override
	public FeaturePresenter getFeaturePresenter() {
		return LiveNationApplication.get().getFeaturePresenter();
	}
	
	public void addFragment(int containerId, Fragment fragment, String tag) {
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(containerId, fragment, tag);
		transaction.commit();
	}

	
}
