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

public abstract class BasePresenter implements StateListener {
	private List<BaseState> activeStates = new ArrayList<BaseState>();
	
	public void addActiveState(BaseState state) {
		Logger.log(getTag(), "Adding active state:" + state.hashCode());
		activeStates.add(state);
	}
	
	private void removeActiveState(BaseState state) {
		Logger.log(getTag(), "Removing active state:" + state.hashCode());
		if (activeStates.contains(state)) {
			activeStates.remove(state);
		}
	}
	
	@Override
	public void onNewState(BaseState state) {
		addActiveState(state);
	}
	
	@Override
	public void onStateReady(BaseState state) {
		removeActiveState(state);
	}
	
	@Override
	public void onStateFailed(int failureCode, BaseState state) {
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
