/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.helpers.LocationHelper.LocationCallback;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.VenuesView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.NearbyVenuesWithEventsParameters;

public class NearbyVenuesPresenter extends
		BasePresenter<VenuesView, NearbyVenuesPresenter.VenuesState> implements
		Presenter<VenuesView>, StateListener<NearbyVenuesPresenter.VenuesState> {
	public static final String INTENT_DATA_KEY = NearbyVenuesPresenter.class
			.getName();

	@Override
	public void initialize(Context context, Bundle args, VenuesView view) {
		VenuesState state = new VenuesState(NearbyVenuesPresenter.this, args,
				view, context);
		state.run();
	}

	@Override
	public void onStateReady(VenuesState state) {
		super.onStateReady(state);
		VenuesView view = state.getView();
		List<Venue> venues = state.getResult();
		view.setVenues(venues);
	}

	@Override
	public void onStateFailed(int failureCode, VenuesState state) {
		super.onStateFailed(failureCode, state);
		// TODO: This
	}

	static class VenuesState extends BaseResultState<ArrayList<Venue>, VenuesView>
			implements LocationCallback,
			LiveNationApiService.GetVenuesApiCallback {
		private final Context context;
		private NearbyVenuesWithEventsParameters params;
		public static final int FAILURE_API_GENERAL = 0;
		public static final int FAILURE_LOCATION = 1;

		public VenuesState(StateListener<VenuesState> listener, Bundle args,
				VenuesView view, Context context) {
			super(listener, args, view);
			this.context = context;
		}
		
		@Override
		public void applyArgs(Bundle args) {
			super.applyArgs(args);
			params = ApiParameters.createNearbyVenueEventsParameters();
			params.setMinimumNumberOfEvents(2);
		}

		@Override
		public void onHasResult(ArrayList<Venue> result) {
			onGetVenues(result);
		}

		@Override
		public void retrieveResult() {
			// TODO: For fun: Allow for lat/lng to be overridden via args bundle
			getLocationHelper().getLocation(context, VenuesState.this);
		}

		@Override
		public void onLocation(double lat, double lng) {
			params.setLocation(lat, lng);
			// params.setSortMethod("start_time");
			getApiService().getNearbyVenuesWithEvents(params, VenuesState.this);
		}

		@Override
		public void onGetVenues(List<Venue> venues) {
			setResult((ArrayList<Venue>) venues);
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
