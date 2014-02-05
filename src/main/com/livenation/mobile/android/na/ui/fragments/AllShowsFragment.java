/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class AllShowsFragment extends LiveNationFragment implements EventsView, FeatureView {
	private EventsView showList = null;
	private FeatureView featured = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View result = inflater.inflate(R.layout.fragment_all_shows, container,
				false);
			
		Fragment featured = new FeaturedFragment();
		Fragment showList = new ShowsListFragment();
		
		addFragment(R.id.fragment_all_shows_container_featured, featured, "featured");
		addFragment(R.id.fragment_all_shows_container_list, showList, "show_list");
		
		this.showList = (EventsView) showList;
		this.featured = (FeatureView) featured;
			
		init();
		
		return result;
	}
	
	@Override
	public void setEvents(List<Event> events) {
		showList.setEvents(events);
	}
	
	@Override
	public void setFeatured(List<Chart> features) {
		//TODO:? so feature..
		featured.setFeatured(features);
	}
	
	private void init() {
		getEventsPresenter().initialize(getActivity(), null, AllShowsFragment.this);	
		getFeaturePresenter().initialize(getActivity(), null, AllShowsFragment.this);
	}
	
}
