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

public class SingleEventPresenter extends BasePresenter<SingleEventPresenter.SingleEventState> implements
		Presenter<SingleEventView>, StateListener<SingleEventPresenter.SingleEventState> {
	public static final String INTENT_DATA_KEY = SingleEventPresenter.class.getName();
	public static final String PARAMETER_EVENT_ID = "event_id";
	
	@Override
	public void initialize(Context context, Bundle args, SingleEventView view) {
		SingleEventState state = new SingleEventState(SingleEventPresenter.this, args, view);
		state.run();
	}

	@Override
	public void onStateReady(SingleEventState state) {
		super.onStateReady(state);
		SingleEventView view = state.getView();
		
		Event event = state.getEvent();
		view.setEvent(event);
	}

	@Override
	public void onStateFailed(int failureCode, SingleEventState state) {
		super.onStateFailed(failureCode, state);
		// TODO: this
	}

	static class SingleEventState extends BaseState<SingleEventView>
			implements LiveNationApiService.GetSingleEventApiCallback {
		private Event event;
		private final SingleEventView view;
		private SingleEventParameters apiParams;

		public static final int FAILURE_API_GENERAL = 0;
		
		public SingleEventState(StateListener<SingleEventState> listener, Bundle args, SingleEventView view) {
			super(listener, args, INTENT_DATA_KEY, view);
			this.view = view;
		}
		
		@Override
		public void run() {
			if (hasResult()) {
				onGetEvent((Event) getResult());
			} else {
				getApiService().getSingleEvent(apiParams, SingleEventState.this);
			}
		}
		
		@Override
		public void applyArgs(Bundle args) {
			super.applyArgs(args);
			apiParams = ApiParameters.createSingleEventParameters();
			if (args.containsKey(SingleEventPresenter.PARAMETER_EVENT_ID)) {
				String eventIdRaw = args.getString(PARAMETER_EVENT_ID);
				long eventId = Event.getNumericEventId(eventIdRaw);
				apiParams.setEventId(eventId);
			}
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
