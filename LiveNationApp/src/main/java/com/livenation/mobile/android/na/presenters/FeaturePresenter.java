package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.TopChartParameters;

import java.util.ArrayList;
import java.util.List;

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

    static class FeatureState extends BaseResultState<ArrayList<Chart>, FeatureView> implements ApiService.BasicApiCallback<List<Chart>> {
        public static final int FAILURE_API_GENERAL = 0;
        public static final int FAILURE_LOCATION = 1;
        private final Context context;

        public FeatureState(StateListener<FeatureState> listener, Bundle args, FeatureView view, Context context) {
            super(listener, args, view);
            this.context = context;
        }

        @Override
        public void onHasResult(ArrayList<Chart> result) {
            onResponse(result);
        }

        @Override
        public void retrieveResult() {
            retrieveCharts(getApiService().getApiConfig().getLat(), getApiService().getApiConfig().getLng());
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

        private void retrieveCharts(double lat, double lng) {
            TopChartParameters params = new TopChartParameters();
            params.setLocation(lat, lng);
            getApiService().getTopCharts(params, FeatureState.this);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            notifyFailed(FAILURE_API_GENERAL);
        }

        @Override
        public void onResponse(List<Chart> response) {
            setResult((ArrayList<Chart>) response);
            notifyReady();
        }
    }
}
