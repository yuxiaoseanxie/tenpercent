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

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.EventParameters;

import java.util.ArrayList;
import java.util.List;

public class EventsPresenter extends BasePresenter<EventsView, EventsPresenter.EventsState> implements Presenter<EventsView>, StateListener<EventsPresenter.EventsState> {
    public static final String INTENT_DATA_KEY = EventsPresenter.class.getName();
    public static final String PARAMETER_EVENT_ID = "event_id";

    @Override
    public void initialize(Context context, Bundle args, EventsView view) {
        EventsState state = new EventsState(EventsPresenter.this, args, view, context);
        state.run();
    }

    @Override
    public void onStateReady(EventsState state) {
        super.onStateReady(state);
        EventsView view = state.getView();
        List<Event> events = state.getResult();
        view.setEvents(events);
    }

    @Override
    public void onStateFailed(int failureCode, EventsState state) {
        super.onStateFailed(failureCode, state);
        //TODO: This
    }

    public Bundle getArgs(int offset, int limit) {
        Bundle args = new Bundle();
        args.putInt(EventsState.ARG_OFFSET_KEY, offset);
        args.putInt(EventsState.ARG_LIMIT_KEY, limit);
        return args;
    }

    static class EventsState extends BaseResultState<ArrayList<Event>, EventsView> implements ApiService.BasicApiCallback<List<Event>> {
        public static final int FAILURE_API_GENERAL = 0;
        public static final int FAILURE_LOCATION = 1;
        private static final String ARG_OFFSET_KEY = "offset";
        private static final String ARG_LIMIT_KEY = "limit";
        private final Context context;
        private EventParameters params;


        public EventsState(StateListener<EventsState> listener, Bundle args, EventsView view, Context context) {
            super(listener, args, view);
            this.context = context;
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            params = new EventParameters();
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
                params = new EventParameters();
            }
            params.setLocation(getApiService().getApiConfig().getLat(), getApiService().getApiConfig().getLng());
            params.setSortMethod("start_time");
            getApiService().getEvents(params, EventsState.this);
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        @Override
        public void onResponse(List<Event> result) {
            //The Java List interface does not implement Serializable, but ArrayList does
            setResult((ArrayList<Event>) result);
            notifyReady();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            notifyFailed(FAILURE_API_GENERAL);
        }
    }
}
