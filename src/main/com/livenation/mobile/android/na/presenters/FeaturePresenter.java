package com.livenation.mobile.android.na.presenters;

import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.helpers.LocationHelper.LocationCallback;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService.GetTopChartsCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.TopChartParameters;

public class FeaturePresenter extends BasePresenter<FeaturePresenter.FeatureState> implements Presenter<FeatureView>, StateListener<FeaturePresenter.FeatureState> {

	@Override
	public void initialize(Context context, Bundle args, FeatureView view) {
		FeatureState state = new FeatureState(FeaturePresenter.this, view);
		state.retrieveLocation(context);
	}
	
	@Override
	public void onStateReady(FeatureState state) {
		super.onStateReady(state);
		FeatureView view = state.getView();
		List<Chart> featured = state.getCharts();
		view.setFeatured(featured);
		
	}
	
	static class FeatureState extends BaseState<FeatureView> implements GetTopChartsCallback, LocationCallback {
		private List<Chart> charts;
		
		public static final int FAILURE_API_GENERAL = 0;
		public static final int FAILURE_LOCATION = 1;
		
		public FeatureState(StateListener<FeatureState> listener, FeatureView view) {
			super(listener, view);
		}
		
		@Override
		public void onGetCharts(List<Chart> charts) {
			this.charts = charts;
			notifyReady();
		}
		
		@Override
		public void onLocation(double lat, double lng) {
			retrieveCharts(lat, lng);
		}

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}
		
		@Override
		public void onLocationFailure(int failureCode) {
			notifyFailed(FAILURE_LOCATION);
		}
		
		public void retrieveLocation(Context context) {
			getLocationHelper().getLocation(context, FeatureState.this);
		}
		
		private void retrieveCharts(double lat, double lng) {
			TopChartParameters params = ApiParameters.createChartParameters();
			params.setLocation(lat, lng);
			getApiService().getTopCharts(params, FeatureState.this);
		}
		
		public List<Chart> getCharts() {
			return charts;
		}

	}
}
