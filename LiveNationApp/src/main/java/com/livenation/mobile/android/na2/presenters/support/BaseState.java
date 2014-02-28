/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.presenters.support;

import java.io.Serializable;

import android.os.Bundle;

import com.livenation.mobile.android.na2.app.LiveNationApplication;
import com.livenation.mobile.android.na2.helpers.LocationHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BaseState<T extends PresenterView> implements Runnable {
	private final StateListener listener;
	
	private final T view;

    private boolean cancelled = false;

	public BaseState(StateListener listener, Bundle args, T view) {
		this.listener = listener;
		this.view = view;
		if (null != args) {
			applyArgs(args);
		}
		listener.onNewState(BaseState.this);
	}

    public void applyArgs(Bundle args) {}

	public T getView() {
		return view;
	}

    public void cancel() {
        this.cancelled = true;
    }

	public void notifyReady() {
		if (null == listener) return;
        if (cancelled) {
            listener.onStateCancelled(this);
        } else {
		    listener.onStateReady(this);
        }
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
        void onStateCancelled(T1 state);
		void onStateFailed(int failureCode, T1 state);
	}
	
}
