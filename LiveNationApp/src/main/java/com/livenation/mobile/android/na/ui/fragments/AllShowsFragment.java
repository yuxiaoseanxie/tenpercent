/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.util.Logger;

import io.segment.android.models.Props;

public class AllShowsFragment extends LiveNationFragment implements FeatureView, ApiServiceBinder {
	private FeatureView featured = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        trackScreenWithLocation("User views All Shows screen", new Props());
        
		Fragment featured = new FeaturedFragment();
		Fragment showList = new ShowsListFragment();
	
		addFragment(R.id.fragment_all_shows_container_featured, featured, "featured");
		addFragment(R.id.fragment_all_shows_container_list, showList, "show_list");

		this.featured = (FeatureView) featured;
		
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_all_shows, container, false);

		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
        Logger.log("ApiBind", "All shows start");
	    LiveNationApplication.get().getApiHelper().persistentBindApi(this);
	}

    @Override
    public void onStop() {
        super.onStop();
        Logger.log("ApiBind", "All shows stop");
        deinit();
        LiveNationApplication.get().getApiHelper().persistentUnbindApi(this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {

        Logger.log("ApiBind", "All shows binded");
        init();
    }

    @Override
	public void setFeatured(List<Chart> features) {	
		getActivity().getIntent().putExtra(FeaturePresenter.INTENT_DATA_KEY, (Serializable) features);
		featured.setFeatured(features);
	}
	
	private void init() {
		Context context = getActivity();
		//Bundle args = getActivity().getIntent().getExtras();

		getFeaturePresenter().initialize(context, null, AllShowsFragment.this);
	}

    private void deinit() {
        getFeaturePresenter().cancel(AllShowsFragment.this);
    }
	
}
