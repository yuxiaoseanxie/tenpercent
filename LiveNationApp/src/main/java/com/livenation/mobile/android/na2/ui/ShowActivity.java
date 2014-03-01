/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.app.LiveNationApplication;
import com.livenation.mobile.android.na2.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na2.presenters.views.SingleEventView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class ShowActivity extends FragmentActivity implements SingleEventView  {
	private SingleEventView singleEventView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show);
		
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		
		getActionBar().setCustomView(R.layout.actionbar_show_custom);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		init();
    }


    @Override
    protected void onStop() {
        super.onStop();
        deinit();
    }

    @Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		if (null == singleEventView) {
			singleEventView = (SingleEventView) getSupportFragmentManager().findFragmentById(R.id.activity_show_content);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_show, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				navigateUp();
				break;
		}
		return true;
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

    private void deinit() {
        getSingleEventPresenter().cancel(ShowActivity.this);
    }
	
	private SingleEventPresenter getSingleEventPresenter() {
		return LiveNationApplication.get().getSingleEventPresenter();
	}

	private void navigateUp() {
		Intent intent = new Intent(ShowActivity.this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

}