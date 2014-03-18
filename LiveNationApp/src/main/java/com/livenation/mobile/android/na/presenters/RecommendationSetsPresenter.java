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
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState.StateListener;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.RecommendationSetsView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.RecommendationSet;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;

import java.util.ArrayList;
import java.util.List;

public class RecommendationSetsPresenter extends BasePresenter<RecommendationSetsView, RecommendationSetsPresenter.RecommendationSetsState> implements Presenter<RecommendationSetsView>, StateListener<RecommendationSetsPresenter.RecommendationSetsState> {
	public static final String INTENT_DATA_KEY = RecommendationSetsPresenter.class.getName();
	public static final String PARAMETER_EVENT_ID = "event_id";

	@Override
	public void initialize(Context context, Bundle args, RecommendationSetsView view) {
        RecommendationSetsState state = new RecommendationSetsState(RecommendationSetsPresenter.this, args, view, context);
		state.run(); 
	}
	
	@Override
	public void onStateReady(RecommendationSetsState state) {
		super.onStateReady(state);
		RecommendationSetsView view = state.getView();
		List<RecommendationSet> result = state.getResult();
		view.setRecommendationSets(result);
	}
	
	@Override
	public void onStateFailed(int failureCode, RecommendationSetsState state) {
		super.onStateFailed(failureCode, state);
		//TODO: This
	}

    public Bundle getArgs(int offset, int limit) {
        Bundle args = new Bundle();
        args.putInt(RecommendationSetsState.ARG_OFFSET_KEY, offset);
        args.putInt(RecommendationSetsState.ARG_LIMIT_KEY, limit);
        return args;
    }

	static class RecommendationSetsState extends BaseResultState<ArrayList<RecommendationSet>, RecommendationSetsView> implements LiveNationApiService.GetRecommendationSetsApiCallback {
        private ApiParameters.RecommendationSetsParameters params;
		private final Context context;
        public static final int FAILURE_API_GENERAL = 0;
		public static final int FAILURE_LOCATION = 1;
        private static final String ARG_OFFSET_KEY = "offset";
        private static final String ARG_LIMIT_KEY = "limit";


        public RecommendationSetsState(StateListener<RecommendationSetsState> listener, Bundle args, RecommendationSetsView view, Context context) {
			super(listener, args, view);
			this.context = context;
		}

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            params = ApiParameters.createRecommendationSetsParameters();
            if (args.containsKey(ARG_OFFSET_KEY) && args.containsKey(ARG_LIMIT_KEY)) {
                int offset = args.getInt(ARG_OFFSET_KEY);
                int limit = args.getInt(ARG_LIMIT_KEY);
                params.setPage(offset, limit);
            }
            params.setIncludes(new String[]{"personal", "popular"});
        }

        @Override
		public void onHasResult(ArrayList<RecommendationSet> result) {
			onGetRecommendationSets(result);
		}
		
		@Override
		public void retrieveResult() {
            if (null == params) {
                params = ApiParameters.createRecommendationSetsParameters();
            }
            params.setLocation(getApiService().getApiConfig().getLat(), getApiService().getApiConfig().getLng());
            params.setRadius(100);
            getApiService().getRecommendationSets(params, RecommendationSetsState.this);
		}

        @Override
        public void onGetRecommendationSets(List<RecommendationSet> result) {
            //The Java List interface does not implement Serializable, but ArrayList does
            setResult((ArrayList<RecommendationSet>) result);
            notifyReady();
        }

		@Override
		public void onFailure(int failureCode, String message) {
			notifyFailed(FAILURE_API_GENERAL);
		}

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }

    }
}
