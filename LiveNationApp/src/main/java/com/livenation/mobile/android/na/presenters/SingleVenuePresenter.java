package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SingleVenueParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

public class SingleVenuePresenter extends
        BasePresenter<SingleVenueView, SingleVenuePresenter.SingleVenueState> implements Presenter<SingleVenueView>,
        StateListener<SingleVenuePresenter.SingleVenueState> {
    private static final String INTENT_DATA_KEY = SingleVenuePresenter.class.getName();
    public static final String PARAMETER_VENUE_ID = "venue_id";

    public static Bundle getAruguments(String venueIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(SingleVenuePresenter.PARAMETER_VENUE_ID, venueIdRaw);
        return bundle;
    }

    public static Bundle getAruguments(Long venueIdRaw) {
        Bundle bundle = new Bundle();
        if (venueIdRaw != null) {
            bundle.putString(SingleVenuePresenter.PARAMETER_VENUE_ID, venueIdRaw.toString());
        }
        return bundle;
    }

    public static void embedResult(Bundle args, Venue venueCache) {
        //It's possible to have a venue without box office info. If
        //handed one of those, we ignore it so that we'll load the
        //full entity from platform.
        if (null != venueCache) {
            args.putSerializable(SingleVenuePresenter.INTENT_DATA_KEY, venueCache);
        }
    }

    @Override
    public void initialize(Context context, Bundle args, SingleVenueView view) {
        SingleVenueState state = new SingleVenueState(SingleVenuePresenter.this, args, view);
        state.run();
    }

    @Override
    public void onStateReady(SingleVenueState state) {
        super.onStateReady(state);
        SingleVenueView view = state.getView();

        Venue venue = state.getResult();
        view.setVenue(venue);
    }

    @Override
    public void onStateFailed(int failureCode, SingleVenueState state) {
        super.onStateFailed(failureCode, state);
        // TODO: this
    }

    static class SingleVenueState extends BaseResultState<Venue, SingleVenueView> implements
            ApiService.BasicApiCallback<Venue> {
        private SingleVenueParameters apiParams;

        public SingleVenueState(StateListener<SingleVenueState> listener, Bundle args, SingleVenueView view) {
            super(listener, args, view);
        }

        @Override
        public void onHasResult(Venue result) {
            onResponse(result);
        }

        @Override
        public void retrieveResult() {
            getApiService().getSingleVenue(apiParams, SingleVenueState.this);
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            apiParams = new SingleVenueParameters();

            String venueIdRaw = args.getString(PARAMETER_VENUE_ID);
            long venueId = DataModelHelper.getNumericEntityId(venueIdRaw);
            apiParams.setVenueId(venueId);
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        @Override
        public void onResponse(Venue result) {
            setResult(result);
            notifyReady();
        }

        @Override
        public void onErrorResponse(LiveNationError error) {
            int errorCode = error.getErrorCode();
            notifyFailed(errorCode);
        }
    }
}
