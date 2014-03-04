/*
 * 
 * @author Charlie Chilton 2014/02/04
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.notifications.ui.InboxActivity;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.ui.FavoriteActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public class AccountFragment extends LiveNationFragment implements AccountUserView {
	private Fragment profileFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_account, container,
				false);
		OnFavoriteClick favoriteArtistOnClick = new OnFavoriteClick(FavoritesFragment.ARG_VALUE_ARTISTS);
		result.findViewById(R.id.account_detail_favorite_artists_container).setOnClickListener(favoriteArtistOnClick);
		
		OnFavoriteClick favoriteVenueOnClick = new OnFavoriteClick(FavoritesFragment.ARG_VALUE_VENUES);
		result.findViewById(R.id.account_detail_favorite_venues_container).setOnClickListener(favoriteVenueOnClick);

        OnNotificationsClick notificationsClick = new OnNotificationsClick();
        result.findViewById(R.id.account_detail_notifications_container).setOnClickListener(notificationsClick);

		return result;
	}
	
	@Override
	public void onResume() {
		super.onResume();
 		getAccountPresenters().getGetUser().initialize(getActivity(), null, AccountFragment.this);
	}
	
	@Override
	public void setUser(User user) {
		if (null != profileFragment) {
			removeFragment(profileFragment);
			profileFragment = null;
		}
		
		if (null == user) {
			profileFragment = new AccountSignInFragment();
		} else {
			profileFragment = new AccountUserFragment();
		}
	
		addFragment(R.id.account_header_provider_container, profileFragment, "account_provider");
	}

	private class OnFavoriteClick implements View.OnClickListener {
		private final int showTab;
		
		public OnFavoriteClick(int showTab) {
			this.showTab = showTab;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(), FavoriteActivity.class);
			intent.putExtra(FavoritesFragment.ARG_SHOW_TAB, showTab);
			startActivity(intent);
		}
	}

    private class OnNotificationsClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent launchInboxIntent = new Intent(getActivity(), InboxActivity.class);
            startActivity(launchInboxIntent);
        }
    }
}
