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
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class AllShowsFragment extends Fragment implements EventsView, FeatureView {
	EventsView showList = null;
	FeatureView featured = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_all_shows, container,
				false);
		this.showList = (EventsView) getFragmentManager().findFragmentById(R.id.fragment_all_shows_show_content);
		this.featured = (FeatureView) getFragmentManager().findFragmentById(R.id.fragment_all_shows_featured_content);
		
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
	
	private EventsPresenter getEventsPresenter() {
		return LiveNationApplication.get().getEventsPresenter();
	}
	
	private FeaturePresenter getFeaturePresenter() {
		return LiveNationApplication.get().getFeaturePresenter();
	}
}
