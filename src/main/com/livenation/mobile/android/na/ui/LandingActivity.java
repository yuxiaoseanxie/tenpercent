/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.TabHost.TabSpec;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.AllShowsFragment;

public class LandingActivity extends FragmentActivity {
	private ActionBarDrawerToggle drawerToggle;
	private FragmentTabHost tabHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_landing);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		DrawerLayout rootView = (DrawerLayout) findViewById(R.id.activity_landing_drawer);
		drawerToggle = new ActionBarDrawerToggle(LandingActivity.this, rootView,
											R.drawable.ic_drawer, 
											R.string.actionbar_drawer_open,
											R.string.actionbar_drawer_close);
		rootView.setDrawerListener(drawerToggle);
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
	    
		tabHost.setup(this, getSupportFragmentManager(),
				R.id.activity_landing_container);
		
		String title = "";
		TabSpec tabSpec;
		
		title = getString(R.string.tab_title_all_shows);
		tabSpec = tabHost.newTabSpec("all_shows");
		tabSpec.setIndicator(title);
		tabHost.addTab(tabSpec,
				AllShowsFragment.class, null);
		
		title = getString(R.string.tab_title_nearby);
		tabSpec = tabHost.newTabSpec("nearby");
		tabSpec.setIndicator(title);
		tabHost.addTab(tabSpec,
				Fragment.class, null);
		
		title = getString(R.string.tab_title_your_shows);
		tabSpec = tabHost.newTabSpec("your_shows");
		tabSpec.setIndicator(title);
		tabHost.addTab(tabSpec,
				Fragment.class, null);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.syncState();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
