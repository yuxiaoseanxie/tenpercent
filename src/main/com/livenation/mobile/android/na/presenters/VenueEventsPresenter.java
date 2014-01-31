package com.livenation.mobile.android.na.presenters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.VenueEventsParameters;

public class VenueEventsPresenter extends
		BasePresenter<VenueEventsPresenter.VenueEventsState> implements Presenter<EventsView>,
		StateListener<VenueEventsPresenter.VenueEventsState> {
	public static final String INTENT_DATA_KEY = VenueEventsPresenter.class.getName();
	public static final String PARAMETER_EVENT_ID = "venue_id";
	public static final String PARAMETER_LIMIT = "limit";
	
	@Override
	public void initialize(Context context, Bundle args, EventsView view) {
		VenueEventsState state = new VenueEventsState(VenueEventsPresenter.this, args, view);
		state.run();
	}

	@Override
	public void onStateReady(VenueEventsState state) {
		super.onStateReady(state);
		
		EventsView view = state.getView();
		List<Event> events = state.getResult();
		view.setEvents(events);
	}

	@Override
	public void onStateFailed(int failureCode, VenueEventsState state) {
		super.onStateFailed(failureCode, state);
		// TODO: this
	}
	
	static class VenueEventsState extends BaseState<ArrayList<Event>, EventsView> implements
			LiveNationApiService.GetEventsApiCallback {
		private VenueEventsParameters apiParams;

		public static final int FAILURE_API_GENERAL = 0;

		public VenueEventsState(StateListener<VenueEventsState> listener, Bundle args, EventsView view) {
			super(listener, args, INTENT_DATA_KEY, view);
		}
		
		@Override
		public void onHasResult(ArrayList<Event> result) {
			onGetEvents(result);
		}

		@Override
		public void retrieveResult() {
			getApiService().getVenueEvents(apiParams, VenueEventsState.this);				
		}

		@Override
		public void applyArgs(Bundle args) {
			super.applyArgs(args);
			apiParams = ApiParameters.createVenueEventsParameters();

			String venueIdRaw = args.getString(PARAMETER_EVENT_ID);
			apiParams.setVenueId(Venue.getNumericVenueId(venueIdRaw));	
			
			if (args.containsKey(PARAMETER_LIMIT)) {
				int limit = args.getInt(PARAMETER_LIMIT);
				apiParams.setPage(0, limit);
			}
		}

		@Override
		public void onGetEvents(List<Event> events) {
			setResult((ArrayList<Event>) events);
			notifyReady();
		}

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}
		
	}
}
