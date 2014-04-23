/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.VenuesView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.NearbyVenuesWithEventsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

public class NearbyVenuesPresenter extends
        BasePresenter<VenuesView, NearbyVenuesPresenter.VenuesState> implements
        Presenter<VenuesView>, StateListener<NearbyVenuesPresenter.VenuesState> {
    public static final String INTENT_DATA_KEY = NearbyVenuesPresenter.class
            .getName();

    @Override
    public void initialize(Context context, Bundle args, VenuesView view) {
        VenuesState state = new VenuesState(NearbyVenuesPresenter.this, args,
                view, context);
        state.run();
    }

    @Override
    public void onStateReady(VenuesState state) {
        super.onStateReady(state);
        VenuesView view = state.getView();
        List<Venue> venues = state.getResult();
        view.setVenues(venues);
    }

    @Override
    public void onStateFailed(int failureCode, VenuesState state) {
        super.onStateFailed(failureCode, state);
        // TODO: This
    }

    public Bundle getArgs(int offset, int limit) {
        Bundle args = new Bundle();
        args.putInt(VenuesState.ARG_OFFSET_KEY, offset);
        args.putInt(VenuesState.ARG_LIMIT_KEY, limit);
        return args;
    }

    static class VenuesState extends BaseResultState<ArrayList<Venue>, VenuesView>
            implements ApiService.BasicApiCallback<List<Venue>> {
        public static final int FAILURE_API_GENERAL = 0;
        public static final int FAILURE_LOCATION = 1;
        private static final String ARG_OFFSET_KEY = "offset";
        private static final String ARG_LIMIT_KEY = "limit";
        private final Context context;
        private NearbyVenuesWithEventsParameters params;

        public VenuesState(StateListener<VenuesState> listener, Bundle args,
                           VenuesView view, Context context) {
            super(listener, args, view);
            this.context = context;
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            params = new NearbyVenuesWithEventsParameters();
            params.setMinimumNumberOfEvents(2);
            if (args.containsKey(ARG_OFFSET_KEY) && args.containsKey(ARG_LIMIT_KEY)) {
                int offset = args.getInt(ARG_OFFSET_KEY);
                int limit = args.getInt(ARG_LIMIT_KEY);
                params.setPage(offset, limit);
            }
        }

        @Override
        public void onHasResult(ArrayList<Venue> result) {
            onResponse(result);
        }

        @Override
        public void retrieveResult() {
            params.setLocation(getApiService().getApiConfig().getLat(), getApiService().getApiConfig().getLng());
            getApiService().getNearbyVenuesWithEvents(params, VenuesState.this);
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        @Override
        public void onErrorResponse(LiveNationError error) {
            notifyFailed(FAILURE_API_GENERAL);
        }

        @Override
        public void onResponse(List<Venue> response) {
            setResult((ArrayList<Venue>) response);
            notifyReady();
        }
    }
}
