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
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.SingleEventParameters;

public class SingleEventPresenter extends BasePresenter implements
		Presenter<SingleEventView>, StateListener {
	public static final String PARAMETER_EVENT_ID = "event_id";

	private SingleEventState state = null;

	@Override
	public void initailize(Context context, Bundle args, SingleEventView view) {
		state = new SingleEventState(SingleEventPresenter.this, view);
		String eventIdRaw = args.getString(PARAMETER_EVENT_ID);
		long eventId = Event.getNumericEventId(eventIdRaw);
		state.retrieveEvent(eventId);
	}

	@Override
	public void onStateReady(BaseState state) {
		super.onStateReady(state);
		SingleEventState singleEventState = (SingleEventState) state;
		SingleEventView view = singleEventState.getView();
		
		Event event = singleEventState.getEvent();
		if (null != view) {
			view.setEvent(event);
		}
	}

	@Override
	public void onStateFailed(int failureCode, BaseState state) {
		super.onStateFailed(failureCode, state);
		// TODO: this
	}

	private static class SingleEventState extends BaseState
			implements LiveNationApiService.GetSingleEventApiCallback {
		private Event event;
		private final SingleEventView view;
		
		public static final int FAILURE_API_GENERAL = 0;
		
		public SingleEventState(StateListener listener, SingleEventView view) {
			super(listener);
			this.view = view;
		}

		public void retrieveEvent(long eventId) {
			SingleEventParameters params = ApiParameters.createSingleEventParameters();
			params.setEventId(eventId);
			getApiService().getSingleEvent(params, SingleEventState.this);
		}

		@Override
		public void onGetEvent(Event result) {
			this.event = result;
			notifyReady();
		}

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}
		
		public Event getEvent() {
			return event;
		}

		public SingleEventView getView() {
			return view;
		}
	}
}
