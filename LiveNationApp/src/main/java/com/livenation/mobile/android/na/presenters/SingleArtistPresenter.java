package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.SingleArtistView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;

public class SingleArtistPresenter
        extends BasePresenter<SingleArtistView, SingleArtistPresenter.SingleArtistState>
        implements Presenter<SingleArtistView>, BaseState.StateListener<SingleArtistPresenter.SingleArtistState> {
    private static final String INTENT_DATA_KEY = SingleArtistPresenter.class.getName();
    private static final String PARAMETER_ARTIST_ID = "artist_id";

    @Override
    public void initialize(Context context, Bundle args, SingleArtistView view) {
        SingleArtistState state = new SingleArtistState(this, args, view);
        state.run();
    }

    @Override
    public void onStateReady(SingleArtistState state) {
        super.onStateReady(state);
        SingleArtistView view = state.getView();

        Artist artist = state.getResult();
        view.setSingleArtist(artist);
    }

    @Override
    public void onStateFailed(int failureCode, SingleArtistState state) {
        super.onStateFailed(failureCode, state);
        //TODO: this?
    }

    public static Bundle getAruguments(String artistIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_ARTIST_ID, artistIdRaw);
        return bundle;
    }

    public static void embedResult(Bundle args, Artist artistCache) {
        if (null != artistCache) {
            args.putSerializable(SingleArtistPresenter.INTENT_DATA_KEY, artistCache);
        }
    }

    public static class SingleArtistState extends BaseResultState<Artist, SingleArtistView> implements LiveNationApiService.GetSingleArtistApiCallback {
        private ApiParameters.SingleArtistParameters apiParams;

        public static final int FAILURE_API_GENERAL = 0;

        public SingleArtistState(StateListener<SingleArtistState> listener, Bundle args, SingleArtistView view) {
            super(listener, args, view);
        }

        @Override
        public void onHasResult(Artist artist) {
            onGetArtist(artist);
        }

        @Override
        public void retrieveResult() {
            getApiService().getSingleArtist(apiParams, SingleArtistState.this);
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            apiParams = ApiParameters.createSingleArtistParameters();
            if(args.containsKey(PARAMETER_ARTIST_ID)) {
                String artistIdRaw = args.getString(PARAMETER_ARTIST_ID);
                long artistId = DataModelHelper.getNumericEntityId(artistIdRaw);
                apiParams.setArtistId(artistId);
            }
        }

        @Override
        public void onGetArtist(Artist artist) {
            setResult(artist);
            notifyReady();
        }

        @Override
        public void onFailure(int errorCode, String message) {
            notifyFailed(FAILURE_API_GENERAL);
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }
    }
}
