/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.support;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;


public class BaseState {
	private StateListener listener = null;
	
	public BaseState(StateListener listener) {
		setStateListener(listener);
		listener.onNewState(BaseState.this);
	}
	
	public void setStateListener(StateListener listener) {
		this.listener = listener;
	}
	
	public void notifyReady() {
		if (null == listener) return;
		listener.onStateReady(this);
	}
	
	public void notifyFailed(int failureCode) {
		if (null == listener) return;
		listener.onStateFailed(failureCode, this);
	}
	
	public LiveNationApiService getApiService() {
		return LiveNationApplication.get().getServiceApi();
	}
	
	public LocationHelper getLocationHelper() {
		return LiveNationApplication.get().getLocationHelper();
	}
	
	public interface StateListener {
		void onNewState(BaseState state);
		void onStateReady(BaseState state);
		void onStateFailed(int failureCode, BaseState state);
	}
	
}
