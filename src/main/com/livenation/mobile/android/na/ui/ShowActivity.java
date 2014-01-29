/*
 * 
 * @author Charlie Chilton 2014/01/22
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
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.util.Logger;

public class ShowActivity extends FragmentActivity implements SingleEventView  {
	private SingleEventView singleEventView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
			
		init();
		
		Intent data = getIntent();
		Logger.log("ShowActivity", "Showing: " + data.getStringExtra(SingleEventPresenter.PARAMETER_EVENT_ID));
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		if (null == singleEventView) {
			singleEventView = (SingleEventView) getSupportFragmentManager().findFragmentById(R.id.activity_show_content);
		}
	}
	
	@Override
	public void setEvent(Event event) {
		if (singleEventView == null) {
			//TODO: Possible race condition?
			return;
		}
		singleEventView.setEvent(event);
	}
	
	private void init() {
		getSingleEventPresenter().initialize(ShowActivity.this, getIntent().getExtras(), ShowActivity.this);		
	}
	
	private SingleEventPresenter getSingleEventPresenter() {
		return LiveNationApplication.get().getSingleEventPresenter();
	}

}
