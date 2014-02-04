/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.AllShowsFragment;

public class LandingActivity extends FragmentActivity {
	private ViewPager viewPager;
	private LandingAdapter viewPagerAdapter;
	private ActionBarDrawerToggle drawerToggle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_landing);
		viewPager = (ViewPager) findViewById(R.id.landing_viewpager);
		viewPagerAdapter = new LandingAdapter(getSupportFragmentManager());
		viewPager.setAdapter(viewPagerAdapter);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		DrawerLayout rootView = (DrawerLayout) findViewById(R.id.activity_landing_drawer);
		drawerToggle = new ActionBarDrawerToggle(LandingActivity.this, rootView,
											R.drawable.ic_drawer, 
											R.string.actionbar_drawer_open,
											R.string.actionbar_drawer_close);
		rootView.setDrawerListener(drawerToggle);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabReselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTabSelected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTabUnselected(Tab tab,
					android.app.FragmentTransaction ft) {
				// TODO Auto-generated method stub

			}

		};
		
		for (int i = 0; i < 3; i++) {
			String text = "";
			switch (i) {
			case 0:
				text = getString(R.string.landing_tab_1).toUpperCase(Locale.US);
				break;
			case 1:
				text = getString(R.string.landing_tab_2).toUpperCase(Locale.US);
				break;
			case 2:
				text = getString(R.string.landing_tab_3).toUpperCase(Locale.US);
				break;
				
			}
			actionBar.addTab(actionBar.newTab().setText(text).setTabListener(tabListener));
		}
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

	public static class LandingAdapter extends FragmentPagerAdapter {

		public LandingAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return new AllShowsFragment();
			} else {
				return new Fragment();
			}
		}

	}
}
