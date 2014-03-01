package com.livenation.mobile.android.na2.presenters.support;

import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;

import java.util.List;

/**
 * Created by cchilton on 2/27/14.
 */
public abstract class BaseObserverPresenter<T extends IdEquals, P extends PresenterView, P2 extends BaseState> extends BasePresenter<P, P2> {
    public abstract void post(T object);
    public abstract void postAll(List<T> object);
    public abstract boolean contains(T object);
    public abstract boolean remove(T object);
    public abstract void clear();
    public abstract T getExisting(T stub);
}
