/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.support;

import java.util.ArrayList;
import java.util.List;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.util.Logger;

@SuppressWarnings("rawtypes")
public abstract class BasePresenter<T extends BaseState> implements StateListener<T> {
	private List<T> activeStates = new ArrayList<T>();
	
	public void addActiveState(T state) {
		Logger.log(getTag(), "Adding active state:" + state.hashCode());
		activeStates.add(state);
	}
	
	private void removeActiveState(T state) {
		Logger.log(getTag(), "Removing active state:" + state.hashCode());
		if (activeStates.contains(state)) {
			activeStates.remove(state);
		} else {
			throw new IllegalStateException("State was never registered..");
		}
	}
	
	@Override
	public void onNewState(T state) {
		addActiveState(state);
	}
	
	@Override
	public void onStateReady(T state) {
		removeActiveState(state);
	}
	
	@Override
	public void onStateFailed(int failureCode, T state) {
		removeActiveState(state);
	}
	
	public LiveNationApiService getApiService() {
		return LiveNationApplication.get().getServiceApi();
	}
	
	public LocationHelper getLocationHelper() {
		return LiveNationApplication.get().getLocationHelper();
	}

	public String getTag() {
		return this.getClass().getSimpleName();
	}
	
}
