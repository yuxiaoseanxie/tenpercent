package com.livenation.mobile.android.na.presenters.support;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by cchilton on 2/27/14.
 */
public abstract class BaseResultState<D extends Serializable, T extends PresenterView> extends BaseState<T> {
    private D result;

    protected BaseResultState(StateListener listener, Bundle args, T view) {
        super(listener, args, view);
    }

    @Override
    public void applyArgs(Bundle args) {
        if (args.containsKey(getDataKey())) {
            result = (D) args.getSerializable(getDataKey());
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

    public void setResult(D result) {
        this.result = result;
    }

    public D getResult() {
        return result;
    }

    public boolean hasResult() {
        return result != null;
    }

    public abstract String getDataKey();

    public abstract void onHasResult(D result);

    public abstract void retrieveResult();
}
