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

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.RecommendationParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsPresenter extends BasePresenter<EventsView, RecommendationsPresenter.RecommendationsState> implements Presenter<EventsView>, StateListener<RecommendationsPresenter.RecommendationsState> {
    public static final String INTENT_DATA_KEY = RecommendationsPresenter.class.getName();

    @Override
    public void initialize(Context context, Bundle args, EventsView view) {
        RecommendationsState state = new RecommendationsState(RecommendationsPresenter.this, args, view);
        state.run();
    }

    @Override
    public void onStateReady(RecommendationsState state) {
        super.onStateReady(state);
        EventsView view = state.getView();
        List<Event> events = state.getResult();
        view.setEvents(events);
    }

    @Override
    public void onStateFailed(int failureCode, RecommendationsState state) {
        super.onStateFailed(failureCode, state);
        //TODO: This
    }

    public Bundle getArgs(int offset, int limit) {
        Bundle args = new Bundle();
        args.putInt(RecommendationsState.ARG_OFFSET_KEY, offset);
        args.putInt(RecommendationsState.ARG_LIMIT_KEY, limit);
        return args;
    }

    static class RecommendationsState extends BaseResultState<ArrayList<Event>, EventsView> implements ApiService.BasicApiCallback<List<Event>> {
        private static final String ARG_OFFSET_KEY = "offset";
        private static final String ARG_LIMIT_KEY = "limit";
        private RecommendationParameters params;


        public RecommendationsState(StateListener<RecommendationsState> listener, Bundle args, EventsView view) {
            super(listener, args, view);
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            params = new RecommendationParameters();
            if (args.containsKey(ARG_OFFSET_KEY) && args.containsKey(ARG_LIMIT_KEY)) {
                int offset = args.getInt(ARG_OFFSET_KEY);
                int limit = args.getInt(ARG_LIMIT_KEY);
                params.setPage(offset, limit);
            }
        }

        @Override
        public void onHasResult(ArrayList<Event> result) {
            onResponse(result);
        }

        @Override
        public void retrieveResult() {
            if (null == params) {
                params = new RecommendationParameters();
            }
            params.setLocation(getApiService().getApiConfig().getLat(), getApiService().getApiConfig().getLng());
            params.setRadius(Constants.DEFAULT_RADIUS);
            getApiService().getRecommendations(params, RecommendationsState.this);
        }


        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        @Override
        public void onErrorResponse(LiveNationError error) {
            int errorCode = error.getErrorCode();
            notifyFailed(errorCode);
        }

        @Override
        public void onResponse(List<Event> response) {
            //The Java List interface does not implement Serializable, but ArrayList does
            setResult((ArrayList<Event>) response);
            notifyReady();
        }
    }
}
