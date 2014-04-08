package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.support.BaseObserverPresenter;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.views.FavoriteObserverView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 2/27/14.
 */
public class FavoriteObserverPresenter extends BaseObserverPresenter<Favorite, FavoriteObserverView, FavoriteObserverPresenter.Observer> implements BaseState.StateListener<FavoriteObserverPresenter.Observer>, ApiServiceBinder {

    private final List<Favorite> favorites = new ArrayList<Favorite>();
    private final static String ARG_FAVORITE_KEY = "favorite";

    public FavoriteObserverPresenter() {
        LiveNationApplication.get().getApiHelper().persistentBindApi(FavoriteObserverPresenter.this);
    }

    @Override
    public void initialize(Context context, Bundle args, FavoriteObserverView view) {
        Observer observer = new Observer(FavoriteObserverPresenter.this, args, view);
        for (Favorite favorite : favorites) {
            if (observing(observer, favorite)) {
                notifyObserver(observer, favorite, false);
            }
        }
        Logger.log("Observer", "Added obs: " + view.hashCode());
    }

    @Override
    public void cancel(FavoriteObserverView view) {
        super.cancel(view);
        Logger.log("Observer", "Removed obs: " + view.hashCode());
    }

    @Override
    public void post(Favorite target) {
        Favorite existing = getExisting(target);
        if (null == existing) {
            //new favorite
            favorites.add(target);
        } else {
            //existing favorite
            favorites.remove(existing);
            favorites.add(target);
        }
        notifyObservers(target, false);
    }

    @Override
    public void postAll(List<Favorite> favorites) {
        for (Favorite favorite : favorites) {
            post(favorite);
        }
    }

    @Override
    public boolean remove(Favorite target) {
        Favorite favorite = getExisting(target);
        if (null == favorite) return false;
        boolean result = favorites.remove(favorite);
        if (result) {
            notifyObservers(favorite, true);
        }
        return result;
    }

    @Override
    public boolean contains(Favorite target) {
        return (null != getExisting(target));
    }

    @Override
    public Favorite getExisting(Favorite target) {
        for (Favorite favorite : favorites) {
            if (favorite.idEquals(target)) {
                return favorite;
            }
        }
        return null;
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        Logger.log("FavoriteObserver", "Attached to API, clearing cache");
        clear();
        List<Favorite> favorites = apiService.getApiConfig().getAppInitResponse().getData().getFavorites();
        postAll(favorites);
    }

    @Override
    public void clear() {
        ArrayList<Favorite> copy = new ArrayList<Favorite>();
        copy.addAll(favorites);
        for (Favorite favorite : copy) {
            remove(favorite);
        }
    }

    public Bundle getBundleArgs(int favoriteTypeId, long itemId) {
        Favorite favorite = new Favorite();
        favorite.setIntType(favoriteTypeId);
        favorite.setId(itemId);

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_FAVORITE_KEY, favorite);
        return bundle;
    }

    private void notifyObservers(Favorite target, boolean removed) {
        for (Observer observer : getStates()) {
            if (observing(observer, target)) {
                notifyObserver(observer, target, removed);
            }
        }
    }

    private boolean observing(Observer observer, Favorite favorite) {
        return observer.getTarget().idEquals(favorite);
    }

    private void notifyObserver(Observer observer, Favorite favorite, boolean removed) {
        if (removed) {
            observer.getView().onFavoriteRemoved(favorite);
        } else {
            observer.getView().onFavoriteAdded(favorite);
        }
    }

    static class Observer extends BaseState<FavoriteObserverView> {
        private Favorite target;

        public Observer(StateListener<Observer> listener,
                        Bundle args, FavoriteObserverView view) {
            super(listener, args, view);
        }

        @Override
        public void run() {
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            target = (Favorite) args.getSerializable(ARG_FAVORITE_KEY);
        }

        public Favorite getTarget() {
            return target;
        }
    }
}
