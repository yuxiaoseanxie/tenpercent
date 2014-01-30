package com.livenation.mobile.android.na.presenters;

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
		List<Event> events = state.getEvents();
		view.setEvents(events);
	}

	@Override
	public void onStateFailed(int failureCode, VenueEventsState state) {
		super.onStateFailed(failureCode, state);
		// TODO: this
	}
	
	static class VenueEventsState extends BaseState<EventsView> implements
			LiveNationApiService.GetEventsApiCallback {
		private List<Event> events;
		private Long venueId;
		private Integer limit;
		
		private final EventsView view;

		public static final int FAILURE_API_GENERAL = 0;

		public VenueEventsState(StateListener<VenueEventsState> listener, Bundle args, EventsView view) {
			super(listener, args, view);
			this.view = view;
		}

		@Override
		public void run() {
			VenueEventsParameters params = ApiParameters.createVenueEventsParameters();
			params.setVenueId(venueId);
			if (null != limit) {
				params.setPage(0, limit);
			}
			getApiService().getVenueEvents(params, VenueEventsState.this);
		}
		
		@Override
		public void applyArgs(Bundle args) {
			String venueIdRaw = args.getString(PARAMETER_EVENT_ID);
			if (args.containsKey(PARAMETER_LIMIT)) {
				limit = args.getInt(PARAMETER_LIMIT);				
			}
			venueId = Venue.getNumericVenueId(venueIdRaw);	
		}

		@Override
		public void onGetEvents(List<Event> events) {
			this.events = events;
			notifyReady();
		}

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}

		public List<Event> getEvents() {
			return events;
		}

		public EventsView getView() {
			return view;
		}
		
	}
}
