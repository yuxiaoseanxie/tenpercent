package com.livenation.mobile.android.na.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.helpers.UiApiSsoProvider;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveUserView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;

public class SsoActivity extends Activity implements ApiSsoProvider.OpenSessionCallback, ApiSsoProvider.GetUserCallback, AccountSaveAuthTokenView, AccountSaveUserView, AccountSignOutView {
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
		Bundle args = getAccountPresenters().getSetAuthToken().getArguments(providerId, sessionToken);
        getAccountPresenters().getSetAuthToken().initialize(SsoActivity.this, args, SsoActivity.this);
	}

	@Override
	public void onSaveAuthTokenSuccess() {
		ssoProvider.getUser(SsoActivity.this);
	}
	
	@Override
	public void onGetUser(User user) {
        if (null == user) return;
		Bundle args = getAccountPresenters().getSetUser().getArguments(user);
		getAccountPresenters().getSetUser().initialize(SsoActivity.this, args, SsoActivity.this);
	}
	
	@Override
	public void onSaveUserSuccess(User user) {
        //bring up the new session in the API
        LiveNationApplication.get().getApiHelper().buildWithSsoProvider(ssoProvider);
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

    @Override
    public void onSignOut() {

    }

    private AccountPresenters getAccountPresenters() {
		return LiveNationApplication.get().getAccountPresenters();
	}
}
