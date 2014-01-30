package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.SingleVenueParameters;

public class SingleVenuePresenter extends
		BasePresenter<SingleVenuePresenter.SingleVenueState> implements Presenter<SingleVenueView>,
		StateListener<SingleVenuePresenter.SingleVenueState> {
	public static final String PARAMETER_EVENT_ID = "venue_id";
	
	@Override
	public void initialize(Context context, Bundle args, SingleVenueView view) {
		SingleVenueState state = new SingleVenueState(SingleVenuePresenter.this, args, view);
		state.run();
	}

	@Override
	public void onStateReady(SingleVenueState state) {
		super.onStateReady(state);
		SingleVenueView view = state.getView();
		
		Venue venue = state.getVenue();
		view.setVenue(venue);
	}

	@Override
	public void onStateFailed(int failureCode, SingleVenueState state) {
		super.onStateFailed(failureCode, state);
		// TODO: this
	}
	
	static class SingleVenueState extends BaseState<SingleVenueView> implements
			LiveNationApiService.GetSingleVenueApiCallback {
		private Venue venue;
		private SingleVenueParameters apiParams;
		
		private final SingleVenueView view;

		public static final int FAILURE_API_GENERAL = 0;

		public SingleVenueState(StateListener<SingleVenueState> listener, Bundle args, SingleVenueView view) {
			super(listener, args, view);
			this.view = view;
		}

		@Override
		public void run() {
			getApiService().getSingleVenue(apiParams, SingleVenueState.this);
		}
		
		@Override
		public void applyArgs(Bundle args) {
			apiParams = ApiParameters.createSingleVenueParameters();

			String venueIdRaw = args.getString(PARAMETER_EVENT_ID);
			long venueId = Venue.getNumericVenueId(venueIdRaw);
			apiParams.setVenueId(venueId);
		}

		@Override
		public void onGetVenue(Venue result) {
			this.venue = result;
			notifyReady();
		}

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}

		public Venue getVenue() {
			return venue;
		}

		public SingleVenueView getView() {
			return view;
		}
		
	}
}
