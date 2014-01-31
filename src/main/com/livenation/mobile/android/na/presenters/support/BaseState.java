/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.support;

import java.io.Serializable;

import android.os.Bundle;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BaseState<T extends PresenterView> implements Runnable {
	private final StateListener listener;
	
	private final T view;
	private final String presenterDataKey;
	
	private Serializable intentData;

	public BaseState(StateListener listener, Bundle args, String presenterDataKey, T view) {
		this.listener = listener;
		this.view = view;
		this.presenterDataKey = presenterDataKey;
		if (null != args) {
			applyArgs(args);
		}
		listener.onNewState(BaseState.this);
	}
	
	public T getView() {
		return view;
	}
	
	public void applyArgs(Bundle args) {
		if (args.containsKey(presenterDataKey)) {
			intentData = args.getSerializable(presenterDataKey);
		}
	};
	
	public boolean hasResult() {
		return intentData != null;
	}
	
	public Serializable getResult() {
		return intentData;
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
	
	public interface StateListener<T1 extends BaseState> {
		void onNewState(T1 state);
		void onStateReady(T1 state);
		void onStateFailed(int failureCode, T1 state);
	}
	
}
