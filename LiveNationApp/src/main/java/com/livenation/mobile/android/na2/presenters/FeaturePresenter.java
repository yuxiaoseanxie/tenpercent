package com.livenation.mobile.android.na2.presenters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na2.helpers.LocationHelper.LocationCallback;
import com.livenation.mobile.android.na2.presenters.support.BasePresenter;
import com.livenation.mobile.android.na2.presenters.support.BaseResultState;
import com.livenation.mobile.android.na2.presenters.support.BaseState;
import com.livenation.mobile.android.na2.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na2.presenters.support.Presenter;
import com.livenation.mobile.android.na2.presenters.views.FeatureView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService.GetTopChartsCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.TopChartParameters;

public class FeaturePresenter extends BasePresenter<FeatureView, FeaturePresenter.FeatureState>
            implements Presenter<FeatureView>, StateListener<FeaturePresenter.FeatureState> {
	public static final String INTENT_DATA_KEY = FeaturePresenter.class.getName();

	@Override
	public void initialize(Context context, Bundle args, FeatureView view) {
		FeatureState state = new FeatureState(FeaturePresenter.this, args, view, context);
		state.run();
	}
	
	@Override
	public void onStateReady(FeatureState state) {
		super.onStateReady(state);
		FeatureView view = state.getView();
		List<Chart> featured = state.getResult();
		view.setFeatured(featured);
	}
	
	static class FeatureState extends BaseResultState<ArrayList<Chart>, FeatureView> implements GetTopChartsCallback, LocationCallback {
		private final Context context;
		
		public static final int FAILURE_API_GENERAL = 0;
		public static final int FAILURE_LOCATION = 1;
		
		public FeatureState(StateListener<FeatureState> listener, Bundle args, FeatureView view, Context context) {
			super(listener, args, view);
			this.context = context;
		}
		
		@Override
		public void onHasResult(ArrayList<Chart> result) {
			onGetCharts(result);
		}
		
		@Override
		public void retrieveResult() {
			getLocationHelper().getLocation(context, FeatureState.this);
		}
		
		@Override
		public void onGetCharts(List<Chart> charts) {
			setResult((ArrayList<Chart>) charts);
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

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        private void retrieveCharts(double lat, double lng) {
			TopChartParameters params = ApiParameters.createChartParameters();
			params.setLocation(lat, lng);
			getApiService().getTopCharts(params, FeatureState.this);
		}

	}
}
