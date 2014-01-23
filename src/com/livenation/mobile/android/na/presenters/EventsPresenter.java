/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters;

import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.helpers.LocationHelper.LocationCallback;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.EventParameters;

public class EventsPresenter extends BasePresenter implements Presenter<EventsView>, StateListener {

	private EventsState state = null;

	@Override
	public void initailize(Context context, Bundle args, EventsView view) {
		state = new EventsState(EventsPresenter.this, view);
		state.retrieveLocation(context); 
	}

	@Override
	public void onStateReady(BaseState state) {
		super.onStateReady(state);
		EventsState eventsState = (EventsState) state;
		EventsView view = eventsState.getView();
		List<Event> events = eventsState.getEvents();
		if (null != view) {
			view.setEvents(events);
		}
	}
	
	@Override
	public void onStateFailed(int failureCode, BaseState state) {
		super.onStateFailed(failureCode, state);
		//TODO: This
	}
	
	@Override
	public String getTag() {
		return EventsPresenter.this.getClass().getSimpleName();
	}

	public static class EventsState extends BaseState implements LocationCallback, LiveNationApiService.GetEventsApiCallback {
		public static final int STATE_FAILURE_LOCATION = 0;
		public static final int STATE_FAILURE_API = 1;
		
		private List<Event> events = null;
		private final EventsView view;
		
		public EventsState(StateListener listener, EventsView view) {
			super(listener);
			this.view = view;
		}
		
		private void retrieveLocation(Context context) {
			getLocationHelper().getLocation(context, EventsState.this);		
		}

		@Override
		public void onLocation(double lat, double lng) {
			EventParameters params = ApiParameters.createEventParameters();
			params.setLocation(lat, lng);
			getApiService().getEvents(params, EventsState.this);
		}
		
		@Override
		public void onGetEvents(List<Event> result) {
			this.events = result;
			notifyReady();
		}

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(STATE_FAILURE_API);
		}
		
		@Override
		public void onLocationFailure(int failureCode) {
			notifyFailed(STATE_FAILURE_LOCATION);
		}
		
		public List<Event> getEvents() {
			return events;
		}

		public EventsView getView() {
			return view;
		}
		
	}
}
