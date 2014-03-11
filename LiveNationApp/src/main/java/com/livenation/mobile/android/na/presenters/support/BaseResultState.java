package com.livenation.mobile.android.na.presenters.support;

import android.os.Bundle;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

import java.io.Serializable;

/**
 * Created by cchilton on 2/27/14.
 */
public abstract class BaseResultState<D extends Serializable, T extends PresenterView> extends BaseState<T> implements ApiServiceBinder {
    private D result;
    private LiveNationApiService apiService;

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
        LiveNationApplication.get().getApiHelper().bindApi(this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        this.apiService = apiService;
        if (hasResult()) {
            onHasResult(result);
        } else {
            retrieveResult();
        }
    }

    public LiveNationApiService getApiService() {
        return apiService;
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
