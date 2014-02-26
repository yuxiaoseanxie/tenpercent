/*
 * 
 * @author Charlie Chilton 2014/01/17
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

public abstract class LiveNationListFragment extends ListFragment implements LiveNationFragmentContract {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
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
	
	@Override
	public NearbyVenuesPresenter getNearbyVenuesPresenter() {
		return LiveNationApplication.get().getNearbyVenuesPresenter();
	}
	
	@Override
	public FavoritesPresenter getFavoritesPresenter() {
		return LiveNationApplication.get().getFavoritesPresenter();
	}
}
