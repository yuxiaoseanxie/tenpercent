/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.support;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.platform.util.Logger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public abstract class BasePresenter<T2 extends PresenterView, T extends BaseState> implements Presenter<T2>, StateListener<T> {
    private List<T> activeStates = new ArrayList<T>();

    private void addActiveState(T state) {
        activeStates.add(state);
    }

    private void removeActiveState(T state) {
        if (activeStates.contains(state)) {
            activeStates.remove(state);
        } else {
            throw new IllegalStateException("State was never registered..");
        }
    }

    @Override
    public void cancel(T2 view) {
        for (T state : activeStates) {
            if (state.getView().equals(view)) {
                state.cancel();
            }
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
    public void onStateCancelled(T state) {
        removeActiveState(state);
    }

    @Override
    public void onStateFailed(int failureCode, T state) {
        removeActiveState(state);
    }

    public List<T> getStates() {
        return activeStates;
    }

    public LocationManager getLocationManager() {
        return LiveNationApplication.get().getLocationManager();
    }

    public String getTag() {
        return this.getClass().getSimpleName();
    }

}
