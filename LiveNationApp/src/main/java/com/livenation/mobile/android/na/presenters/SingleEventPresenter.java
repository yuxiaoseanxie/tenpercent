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
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SingleEventParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

public class SingleEventPresenter extends BasePresenter<SingleEventView, SingleEventPresenter.SingleEventState> implements
        Presenter<SingleEventView>, StateListener<SingleEventPresenter.SingleEventState> {
    private static final String INTENT_DATA_KEY = SingleEventPresenter.class.getName();
    private static final String PARAMETER_EVENT_ID = "event_id";

    public static Bundle getAruguments(String eventIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(SingleEventPresenter.PARAMETER_EVENT_ID, eventIdRaw);
        return bundle;
    }

    public static void embedResult(Bundle args, Event eventCache) {
        if (null != eventCache) {
            args.putSerializable(SingleEventPresenter.INTENT_DATA_KEY, eventCache);
        }
    }

    @Override
    public void initialize(Context context, Bundle args, SingleEventView view) {
        SingleEventState state = new SingleEventState(SingleEventPresenter.this, args, view);
        state.run();
    }

    @Override
    public void onStateReady(SingleEventState state) {
        super.onStateReady(state);
        SingleEventView view = state.getView();

        Event event = state.getResult();
        view.setEvent(event);
    }

    @Override
    public void onStateFailed(int failureCode, SingleEventState state) {
        super.onStateFailed(failureCode, state);
        // TODO: this
    }

    static class SingleEventState extends BaseResultState<Event, SingleEventView>
            implements ApiService.BasicApiCallback<Event> {
        public static final int FAILURE_API_GENERAL = 0;
        private SingleEventParameters apiParams;

        public SingleEventState(StateListener<SingleEventState> listener, Bundle args, SingleEventView view) {
            super(listener, args, view);
        }

        @Override
        public void onHasResult(Event result) {
            onResponse(result);
        }

        @Override
        public void retrieveResult() {
            getApiService().getSingleEvent(apiParams, SingleEventState.this);
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            apiParams = new SingleEventParameters();
            if (args.containsKey(SingleEventPresenter.PARAMETER_EVENT_ID)) {
                String eventIdRaw = args.getString(PARAMETER_EVENT_ID);
                long eventId = DataModelHelper.getNumericEntityId(eventIdRaw);
                apiParams.setEventId(eventId);
            }
        }

        @Override
        public void onResponse(Event result) {
            setResult(result);
            notifyReady();
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        @Override
        public void onErrorResponse(LiveNationError error) {
            notifyFailed(FAILURE_API_GENERAL);
        }
    }
}
