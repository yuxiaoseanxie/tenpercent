package com.livenation.mobile.android.na.presenters.support;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Created by cchilton on 2/27/14.
 */
public abstract class BaseResultState<TResult extends Serializable, TView extends PresenterView> extends BaseState<TView> {
    private TResult result;

    protected BaseResultState(StateListener listener, Bundle args, TView view) {
        super(listener, args, view);
    }

    @Override
    public void applyArgs(Bundle args) {
        if (args.containsKey(getDataKey())) {
            result = (TResult) args.getSerializable(getDataKey());
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

    public TResult getResult() {
        return result;
    }

    public void setResult(TResult result) {
        this.result = result;
    }

    public boolean hasResult() {
        return result != null;
    }

    public abstract String getDataKey();

    public abstract void onHasResult(TResult result);

    public abstract void retrieveResult();
}
