package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteAddView;
import com.livenation.mobile.android.na.presenters.views.FavoriteRemoveView;
import com.livenation.mobile.android.na.presenters.views.FavoritesView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.SingleVenueParameters;

import java.util.ArrayList;
import java.util.List;

public class FavoritesPresenter extends
        BasePresenter<FavoritesView, FavoritesPresenter.FavoritesState> implements Presenter<FavoritesView>,
        StateListener<FavoritesPresenter.FavoritesState> {
    public static final String INTENT_DATA_KEY = FavoritesPresenter.class.getName();
    public static final String PARAMETER_EVENT_ID = "item_id";
    private AddFavoritePresenter addFavoritePresenter = new AddFavoritePresenter();
    private RemoveFavoritePresenter removeFavoritePresenter = new RemoveFavoritePresenter();
    private FavoriteObserverPresenter favoriteObserverPresenter = new FavoriteObserverPresenter();

    @Override
    public void initialize(Context context, Bundle args, FavoritesView view) {
        FavoritesState state = new FavoritesState(FavoritesPresenter.this, args, view);
        state.run();
    }

    @Override
    public void onStateReady(FavoritesState state) {
        super.onStateReady(state);
        FavoritesView view = state.getView();

        List<Favorite> result = state.getResult();

        postResult(state);

        view.setFavorites(result);
    }

    @Override
    public void onStateCancelled(FavoritesState state) {
        super.onStateCancelled(state);
        postResult(state);
    }

    @Override
    public void onStateFailed(int failureCode, FavoritesState state) {
        super.onStateFailed(failureCode, state);
        // TODO: this
    }

    public void addFavorite(Context context, Bundle args, FavoriteAddView view) {
        addFavoritePresenter.initialize(context, args, view);
    }

    public void removeFavorite(Context context, Bundle args, FavoriteRemoveView view) {
        removeFavoritePresenter.initialize(context, args, view);
    }

    public FavoriteObserverPresenter getObserverPresenter() {
        return favoriteObserverPresenter;
    }

    public Bundle getArgsBundle(Favorite favorite) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(INTENT_DATA_KEY, favorite);
        return bundle;
    }

    private void postResult(FavoritesState state) {
        List<Favorite> result = state.getResult();

        getObserverPresenter().clear();
        getObserverPresenter().postAll(result);
    }

    static class FavoritesState extends BaseResultState<ArrayList<Favorite>, FavoritesView> implements
            ApiService.BasicApiCallback<List<Favorite>> {
        public static final int FAILURE_API_GENERAL = 0;
        private SingleVenueParameters apiParams;

        public FavoritesState(StateListener<FavoritesState> listener, Bundle args, FavoritesView view) {
            super(listener, args, view);
        }

        @Override
        public void onHasResult(ArrayList<Favorite> result) {
            onResponse(result);
        }

        @Override
        public void retrieveResult() {
            getApiService().getFavorites(FavoritesState.this);
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            apiParams = ApiParameters.createSingleVenueParameters();

            String venueIdRaw = args.getString(PARAMETER_EVENT_ID);
            long venueId = DataModelHelper.getNumericEntityId(venueIdRaw);
            apiParams.setVenueId(venueId);
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            notifyFailed(FAILURE_API_GENERAL);
        }

        @Override
        public void onResponse(List<Favorite> response) {
            setResult((ArrayList<Favorite>) response);
            notifyReady();
        }
    }

    private class AddFavoritePresenter extends
            BasePresenter<FavoriteAddView, AddFavoritePresenter.AddFavoriteState> implements
            Presenter<FavoriteAddView>,
            StateListener<AddFavoritePresenter.AddFavoriteState> {

        @Override
        public void initialize(Context context, Bundle args,
                               FavoriteAddView view) {
            AddFavoriteState state = new AddFavoriteState(AddFavoritePresenter.this, args, view);
            state.run();
        }

        @Override
        public void onStateReady(AddFavoriteState state) {
            super.onStateReady(state);
            FavoriteAddView view = state.getView();

            postResult(state);

            view.onFavoriteAddSuccess();
        }

        @Override
        public void onStateCancelled(AddFavoriteState state) {
            super.onStateCancelled(state);
            postResult(state);
        }

        @Override
        public void onStateFailed(int failureCode, AddFavoriteState state) {
            super.onStateFailed(failureCode, state);
            FavoriteAddView view = state.getView();
            view.onFavoriteAddFailed();
        }

        private void postResult(AddFavoriteState state) {
            Favorite result = state.getResult();
            getObserverPresenter().post(result);
        }

        class AddFavoriteState extends BaseResultState<Favorite, FavoriteAddView> implements
                ApiService.BasicApiCallback<Void> {
            public static final int FAILURE_API_GENERAL = 0;
            private ApiParameters.FavoriteWithNameParameters apiParams;

            public AddFavoriteState(StateListener<AddFavoriteState> listener, Bundle args, FavoriteAddView view) {
                super(listener, args, view);
            }

            @Override
            public void onHasResult(Favorite result) {
                retrieveResult();
            }

            @Override
            public void retrieveResult() {
                getApiService().addFavorite(apiParams, AddFavoriteState.this);
            }

            @Override
            public void applyArgs(Bundle args) {
                super.applyArgs(args);
                apiParams = ApiParameters.createFavoriteWithNameParameters();
                Favorite favorite = (Favorite) args.getSerializable(INTENT_DATA_KEY);
                String idValue = Long.valueOf(favorite.getId()).toString();
                apiParams.setId(idValue, favorite.getType());
                apiParams.setName(favorite.getName());
            }

            @Override
            public String getDataKey() {
                return INTENT_DATA_KEY;
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                notifyFailed(FAILURE_API_GENERAL);
            }

            @Override
            public void onResponse(Void response) {
                notifyReady();
            }
        }
    }

    private class RemoveFavoritePresenter extends
            BasePresenter<FavoriteRemoveView, RemoveFavoritePresenter.RemoveFavoriteState> implements
            Presenter<FavoriteRemoveView>,
            StateListener<RemoveFavoritePresenter.RemoveFavoriteState> {

        @Override
        public void initialize(Context context, Bundle args,
                               FavoriteRemoveView view) {
            RemoveFavoriteState state = new RemoveFavoriteState(
                    RemoveFavoritePresenter.this, args, view);
            state.run();
        }

        @Override
        public void onStateReady(RemoveFavoriteState state) {
            super.onStateReady(state);
            FavoriteRemoveView view = state.getView();

            postResult(state);

            view.onFavoriteRemoveSuccess();
        }

        @Override
        public void onStateCancelled(RemoveFavoriteState state) {
            super.onStateCancelled(state);
            postResult(state);
        }

        @Override
        public void onStateFailed(int failureCode, RemoveFavoriteState state) {
            super.onStateFailed(failureCode, state);
            FavoriteRemoveView view = state.getView();
            view.onFavoriteRemoveFailed();
        }

        private void postResult(RemoveFavoriteState state) {
            Favorite result = state.getResult();
            getObserverPresenter().remove(result);
        }

        class RemoveFavoriteState extends
                BaseResultState<Favorite, FavoriteRemoveView> implements
                ApiService.BasicApiCallback<Void> {
            public static final int FAILURE_API_GENERAL = 0;
            private ApiParameters.FavoriteParameters apiParams;

            public RemoveFavoriteState(StateListener<RemoveFavoriteState> listener,
                                       Bundle args, FavoriteRemoveView view) {
                super(listener, args, view);
            }

            @Override
            public void onHasResult(Favorite result) {
                retrieveResult();
            }

            @Override
            public void retrieveResult() {
                getApiService().removeFavorite(apiParams, RemoveFavoriteState.this);
            }

            @Override
            public void applyArgs(Bundle args) {
                super.applyArgs(args);
                apiParams = ApiParameters.createFavoriteParameters();
                Favorite favorite = (Favorite) args
                        .getSerializable(INTENT_DATA_KEY);
                String idValue = Long.valueOf(favorite.getId()).toString();
                apiParams.setId(idValue, favorite.getType());
            }

            @Override
            public String getDataKey() {
                return INTENT_DATA_KEY;
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                notifyFailed(FAILURE_API_GENERAL);
            }

            @Override
            public void onResponse(Void response) {
                notifyReady();
            }
        }
    }
}
