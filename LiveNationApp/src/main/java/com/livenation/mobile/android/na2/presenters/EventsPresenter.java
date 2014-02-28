/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.presenters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.helpers.LocationHelper.LocationCallback;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.EventParameters;

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

	static class EventsState extends BaseResultState<ArrayList<Event>, EventsView> implements LocationCallback, LiveNationApiService.GetEventsApiCallback {
		private final Context context;
		
		public static final int FAILURE_API_GENERAL = 0;
		public static final int FAILURE_LOCATION = 1;	
		
		public EventsState(StateListener<EventsState> listener, Bundle args, EventsView view, Context context) {
			super(listener, args, view);
			this.context = context;
		}
		
		@Override
		public void onHasResult(ArrayList<Event> result) {
			onGetEvents(result);
		}
		
		@Override
		public void retrieveResult() {
			//TODO: For fun: Allow for lat/lng to be overridden via args bundle
			getLocationHelper().getLocation(context, EventsState.this);		
		}
		
		@Override
		public void onLocation(double lat, double lng) {
			EventParameters params = ApiParameters.createEventParameters();
			params.setLocation(lat, lng);
			params.setSortMethod("start_time");
			getApiService().getEvents(params, EventsState.this);
		}
		
		@Override
		public void onGetEvents(List<Event> result) {
			//The Java List interface does not implement Serializable, but ArrayList does
			setResult((ArrayList<Event>) result);
			notifyReady();
		}

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}
		
		@Override
		public void onLocationFailure(int failureCode) {
			notifyFailed(FAILURE_LOCATION);
		}

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }
    }
}
