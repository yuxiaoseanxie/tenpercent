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
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.ShowFragment;
import com.livenation.mobile.android.platform.util.Logger;

public class ShowActivity extends FragmentActivity  {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		Intent data = getIntent();
		
		Logger.log("ShowActivity", "Showing: " + data.getStringExtra(ShowFragment.PARAMETER_EVENT_ID));
	}

}
