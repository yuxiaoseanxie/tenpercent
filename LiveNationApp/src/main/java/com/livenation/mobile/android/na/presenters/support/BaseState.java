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
public abstract class BaseState<D extends Serializable, T extends PresenterView> implements Runnable {
	private final StateListener listener;
	
	private final T view;
	private final String presenterDataKey;
	
	private D result;
    private boolean cancelled = false;

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
			result = (D) args.getSerializable(presenterDataKey);
		}
	}
	
	@Override
	public final void run() {
		if (hasResult()) {
			onHasResult(result);
		} else {
			retrieveResult();
		}
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
	
	public void setResult(D result) {
		this.result = result;
	}
	
	public D getResult() {
		return result;
	}
	
	public boolean hasResult() {
		return result != null;
	}
	
	public abstract void onHasResult(D result);
	
	public abstract void retrieveResult();
	
	public interface StateListener<T1 extends BaseState> {
		void onNewState(T1 state);
		void onStateReady(T1 state);
        void onStateCancelled(T1 state);
		void onStateFailed(int failureCode, T1 state);
	}
	
}
