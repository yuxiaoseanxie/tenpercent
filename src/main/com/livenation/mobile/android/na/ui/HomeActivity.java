/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.UiApiSsoProvider;
import com.livenation.mobile.android.na.presenters.AccountPresenter;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.na.ui.fragments.AllShowsFragment;
import com.livenation.mobile.android.na.ui.fragments.NearbyVenuesFragment;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.util.Logger;

public class HomeActivity extends FragmentActivity implements AccountSaveAuthTokenView, AccountSignOutView {
	private ActionBarDrawerToggle drawerToggle;
	private FragmentTabHost tabHost;
	private static final int RC_SSO_REPAIR = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_landing);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		DrawerLayout rootView = (DrawerLayout) findViewById(R.id.activity_landing_drawer);
		drawerToggle = new ActionBarDrawerToggle(HomeActivity.this, rootView,
											R.drawable.ic_drawer, 
											R.string.actionbar_drawer_open,
											R.string.actionbar_drawer_close);
		rootView.setDrawerListener(drawerToggle);
		tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
	    
		tabHost.setup(this, getSupportFragmentManager(),
				R.id.activity_landing_container);
		
		String title;
		View view;
		TabSpec tabSpec;
		
		title = getString(R.string.tab_title_all_shows);
		view = createTab(HomeActivity.this, title);
		tabSpec = tabHost.newTabSpec("all_shows");
		tabSpec.setIndicator(view);
		tabHost.addTab(tabSpec,
				AllShowsFragment.class, null);
		
		title = getString(R.string.tab_title_nearby);
		view = createTab(HomeActivity.this, title);
		tabSpec = tabHost.newTabSpec("nearby");
		tabSpec.setIndicator(view);
		tabHost.addTab(tabSpec,
				NearbyVenuesFragment.class, null);
		
		title = getString(R.string.tab_title_your_shows);
		view = createTab(HomeActivity.this, title);
		tabSpec = tabHost.newTabSpec("your_shows");
		tabSpec.setIndicator(view);
		tabHost.addTab(tabSpec, Fragment.class, null);
		
		ApiSsoProvider ssoProvider = getConfiguredSsoProvider();
		if (null != ssoProvider) {
			int providerId = ssoProvider.getId();
			ssoProvider.openSession(false, new TokenUpdater(providerId));
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RC_SSO_REPAIR: 
				if (resultCode != RESULT_OK) {
					//the attempt to fix the SSO config with the user failed, lets wipe the auth configuration.
					getAccountPresenter().getSignOutPresenter().initialize(HomeActivity.this, null, HomeActivity.this);
					//finish the app. this will reset any tokens in memory.
					//alternatively, the serviceApi.setSsoProvider() could be set to null here, but lets not try to be clever.
					finish();
				}
				break;
		}
	}
	
	@Override
	public void onSaveAuthTokenSuccess() {
		Logger.log("AuthToken", "Updated it");
	}
	
	@Override
	public void onSignOut() {
		Logger.log("Account", "Signed out");
	}
	
	@Override
	public void onSaveAuthTokenFailure() {
		throw new IllegalStateException("Should not happen..");
	}
	
	private UiApiSsoProvider getConfiguredSsoProvider() {
		UiApiSsoProvider provider = LiveNationApplication.get().getSsoManager().getConfiguredSsoProvider(HomeActivity.this);
		return provider;
	}
	
	/**
	 * Here we have to return our own Tab View object to get our desired LiveNation red tab.
	 * 
	 * Because Google forgot to make the default tabs in the TabHost XML stylable....
	 * 
	 */
	private View createTab(Context context, String title) {
		View view = LayoutInflater.from(context).inflate(R.layout.view_tab, null);
		TextView text = (TextView) view.findViewWithTag("titleText");
		text.setText(title);
		return view;
	}
	
	
	private AccountPresenter getAccountPresenter() {
		return LiveNationApplication.get().getAccountPresenter();
	}

	/**
	 * Token Updater class.
	 * 
	 * This ensures that the SSO access token being passed to the LN api is current and correct.
	 *
	 */
	private class TokenUpdater implements ApiSsoProvider.OpenSessionCallback {
		private final int providerId;

		public TokenUpdater(int providerId) {
			this.providerId = providerId;
		}
		
		@Override
		public void onOpenSession(String sessionToken) {
			Bundle args = getAccountPresenter().getSetAuthTokenPresenter().getArgumentsBundle(providerId, sessionToken);
			getAccountPresenter().getSetAuthTokenPresenter().initialize(HomeActivity.this, args, HomeActivity.this);
		}

		@Override
		public void onOpenSessionFailed(Exception exception, boolean allowForeground) {
			//possible SSO configuration problem. 
			//Lets give control to whatever SSO SDK it is, and allow it to create whatever
			//foreground windows for user input to resolve.
			ApiSsoProvider ssoProvider = getConfiguredSsoProvider();
			if (null == ssoProvider) return;
			//if the session failed, and it was only allowed to run in the background..
			if (!allowForeground) {
				//Lets try again, but this time with foreground activities that may resolve the session error
				Intent intent = new Intent(HomeActivity.this, SsoActivity.class);
				intent.putExtra(SsoActivity.ARG_PROVIDER_ID, ssoProvider.getId());
				startActivityForResult(intent, RC_SSO_REPAIR);
			}
		}
		
		@Override
		public void onNoNetwork() {
			//do nothing
		}

	}
	
}
