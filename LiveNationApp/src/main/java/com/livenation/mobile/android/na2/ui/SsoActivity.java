package com.livenation.mobile.android.na2.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.livenation.mobile.android.na2.app.LiveNationApplication;
import com.livenation.mobile.android.na2.helpers.SsoManager;
import com.livenation.mobile.android.na2.helpers.UiApiSsoProvider;
import com.livenation.mobile.android.na2.presenters.AccountPresenters;
import com.livenation.mobile.android.na2.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na2.presenters.views.AccountSaveUserView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;

public class SsoActivity extends Activity implements ApiSsoProvider.OpenSessionCallback, ApiSsoProvider.GetUserCallback, AccountSaveAuthTokenView, AccountSaveUserView {
	private UiApiSsoProvider ssoProvider;
	public static final String ARG_PROVIDER_ID = "provider_id";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Integer providerId = null;
		if (getIntent().hasExtra(ARG_PROVIDER_ID)) {
			providerId = getIntent().getExtras().getInt(ARG_PROVIDER_ID);
		}
		
		if (null == providerId) {
			throw new IllegalArgumentException("Which SSO do you want me to sign in?!");
		}
		
		ssoProvider = LiveNationApplication.get().getSsoManager().getSsoProvider(providerId, SsoActivity.this);
		ssoProvider.openSession(true, SsoActivity.this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ssoProvider.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public void onOpenSession(String sessionToken) {
		int providerId = SsoManager.getProviderId(ssoProvider);
		Bundle args = getAccountPresenter().getSetAuthToken().getArguments(providerId, sessionToken);
		getAccountPresenter().getSetAuthToken().initialize(SsoActivity.this, args, SsoActivity.this);
	}

	@Override
	public void onSaveAuthTokenSuccess() {
		ssoProvider.getUser(SsoActivity.this);
	}
	
	@Override
	public void onGetUser(User user) {
		Bundle args = getAccountPresenter().getSetUser().getArguments(user);
		getAccountPresenter().getSetUser().initialize(SsoActivity.this, args, SsoActivity.this);
	}
	
	@Override
	public void onSaveUserSuccess(User user) {
		setResult(RESULT_OK);
		finish();
	}

	@Override
	public void onSaveAuthTokenFailure() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onSaveUserFailure() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onOpenSessionFailed(Exception exception, boolean allowForeground) {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onNoNetwork() {
		setResult(RESULT_OK);
		finish();
	}
	
	private AccountPresenters getAccountPresenter() {
		return LiveNationApplication.get().getAccountPresenters();
	}
}
