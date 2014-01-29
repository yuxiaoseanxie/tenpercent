/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.VenueEventsPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.fragments.VenueFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.util.Logger;

public class VenueActivity extends FragmentActivity implements SingleVenueView, EventsView  {
	private SingleVenueView singleVenueView;
	private EventsView eventsView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_venue);
			
		init();
		
		Intent data = getIntent();
		Logger.log("VenueActivity", "Showing: " + data.getStringExtra(VenueFragment.PARAMETER_VENUE_ID));
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		if (null == singleVenueView) {
			singleVenueView = (SingleVenueView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
		}
		if (null == eventsView) {
			eventsView = (EventsView) getSupportFragmentManager().findFragmentById(R.id.activity_venue_content);
		}
	}
	
	@Override
	public void setVenue(Venue venue) {
		if (singleVenueView == null) {
			//TODO: this
			throw new RuntimeException("TODO: investigate possible race condition here");
		}
		singleVenueView.setVenue(venue);
	}
	
	@Override
	public void setEvents(List<Event> events) {
		if (eventsView == null) {
			//TODO: this
			throw new RuntimeException("TODO: investigate possible race condition here");
		}
		eventsView.setEvents(events);
	}
	
	
	private void init() {
		getSingleVenuePresenter().initialize(VenueActivity.this, getIntent().getExtras(), VenueActivity.this);	
		getVenueEventPresenter().initialize(VenueActivity.this, getIntent().getExtras(), VenueActivity.this);	
	}
	
	private SingleVenuePresenter getSingleVenuePresenter() {
		return LiveNationApplication.get().getSingleVenuePresenter();
	}
	
	private VenueEventsPresenter getVenueEventPresenter() {
		return LiveNationApplication.get().getVenueEventsPresenter();
	}
	
}
