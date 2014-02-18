/*
 * 
 * @author Charlie Chilton 2014/02/04
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.models.User;
import com.livenation.mobile.android.na.presenters.AccountPresenter;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;

public class AccountFragment extends LiveNationFragment implements AccountUserView {
	private AccountPresenter accountProviderPresenter = new AccountPresenter();
	private Fragment profileFragment;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_account, container,
				false);
		return result;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		accountProviderPresenter.initialize(getActivity(), null, AccountFragment.this);
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
			
			Bundle args = AccountPresenter.getArgumentsBundle(user);
			profileFragment.setArguments(args);
		}
	
		addFragment(R.id.account_header_provider_container, profileFragment, "account_provider");
	}

}
