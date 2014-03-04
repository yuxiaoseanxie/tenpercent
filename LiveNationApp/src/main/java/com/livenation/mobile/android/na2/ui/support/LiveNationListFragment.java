/*
 * 
 * @author Charlie Chilton 2014/01/17
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.ui.support;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na2.app.LiveNationApplication;
import com.livenation.mobile.android.na2.helpers.LocationHelper;
import com.livenation.mobile.android.na2.presenters.AccountPresenters;
import com.livenation.mobile.android.na2.presenters.EventsPresenter;
import com.livenation.mobile.android.na2.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na2.presenters.FeaturePresenter;
import com.livenation.mobile.android.na2.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na2.presenters.SingleEventPresenter;
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

    @Override
    public AccountPresenters getAccountPresenters() {
        return LiveNationApplication.get().getAccountPresenters();
    }

    @Override
    public SingleEventPresenter getSingleEventPresenter() {
        return LiveNationApplication.get().getSingleEventPresenter();
    }
}
