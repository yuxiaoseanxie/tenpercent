/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.presenters.views.SingleVenueView;
import com.livenation.mobile.android.na.ui.fragments.VenueFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.util.Logger;

public class VenueActivity extends FragmentActivity implements SingleVenueView  {
	private SingleVenueView singleVenueView;
	
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
	}
	
	@Override
	public void setVenue(Venue venue) {
		if (singleVenueView == null) {
			//TODO: this
			throw new RuntimeException("TODO: investigate possible race condition here");
		}
		singleVenueView.setVenue(venue);
	}
	
	private void init() {
		getSingleVenuePresenter().initialize(VenueActivity.this, getIntent().getExtras(), VenueActivity.this);		
	}
	
	private SingleVenuePresenter getSingleVenuePresenter() {
		return LiveNationApplication.get().getSingleVenuePresenter();
	}

}
