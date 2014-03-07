/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

public abstract class LiveNationFragment extends Fragment implements LiveNationFragmentContract, StateEnhancer {
	
	private Bundle state;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (null != state) {
			applyInstanceState(state);
		}
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

    @Override
	public void onDestroyView() {
		super.onDestroyView();
		state = new Bundle();
		onSaveInstanceState(state);
	}
	
	@Override
	public void applyInstanceState(Bundle state) {}
	
		
	public void addFragment(int containerId, Fragment fragment, String tag) {
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(containerId, fragment, tag);
		transaction.commit();
	}
	
	public void removeFragment(Fragment fragment) {
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.remove(fragment);
		transaction.commit();
	}
	
	public String getViewKey(View view) {
		return Integer.valueOf(view.getId()).toString();
	}

	
}