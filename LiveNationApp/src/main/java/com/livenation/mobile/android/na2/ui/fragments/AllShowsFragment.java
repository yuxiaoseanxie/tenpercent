/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.ui.fragments;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.presenters.EventsPresenter;
import com.livenation.mobile.android.na2.presenters.FeaturePresenter;
import com.livenation.mobile.android.na2.presenters.views.EventsView;
import com.livenation.mobile.android.na2.presenters.views.FeatureView;
import com.livenation.mobile.android.na2.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class AllShowsFragment extends LiveNationFragment implements EventsView, FeatureView {
	private EventsView showList = null;
	private FeatureView featured = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		Fragment featured = new FeaturedFragment();
		Fragment showList = new ShowsListFragment();
	
		addFragment(R.id.fragment_all_shows_container_featured, featured, "featured");
		addFragment(R.id.fragment_all_shows_container_list, showList, "show_list");
	
		this.showList = (EventsView) showList;
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
		init();
	}

    @Override
    public void onStop() {
        super.onStop();
        deinit();
    }

    @Override
	public void setEvents(List<Event> events) {
		getActivity().getIntent().putExtra(EventsPresenter.INTENT_DATA_KEY, (Serializable) events);
		showList.setEvents(events);
	}
	
	@Override
	public void setFeatured(List<Chart> features) {	
		getActivity().getIntent().putExtra(FeaturePresenter.INTENT_DATA_KEY, (Serializable) features);
		featured.setFeatured(features);
	}
	
	private void init() {
		Context context = getActivity();
		Bundle args = getActivity().getIntent().getExtras();
		
		getEventsPresenter().initialize(context, args, AllShowsFragment.this);	
		getFeaturePresenter().initialize(context, args, AllShowsFragment.this);
	}

    private void deinit() {
        getEventsPresenter().cancel(AllShowsFragment.this);
        getFeaturePresenter().cancel(AllShowsFragment.this);
    }
	
}
