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

import java.util.ArrayList;
import java.util.Iterator;
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
        Iterator<T> iterator = activeStates.iterator();
        while (iterator.hasNext()) {
            T state = iterator.next();
            if (state.getView().equals(view)) {
                state.cancel();
                iterator.remove();
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
        //do nothing
    }

    @Override
    public void onStateFailed(int failureCode, T state) {
        removeActiveState(state);
    }

    public List<T> getStates() {
        return activeStates;
    }

    public String getTag() {
        return this.getClass().getSimpleName();
    }

}
