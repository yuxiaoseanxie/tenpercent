package com.livenation.mobile.android.na.ui;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.livenation.mobile.android.na.helpers.FacebookSsoProvider;
import com.livenation.mobile.android.na.models.User;
import com.livenation.mobile.android.na.presenters.AccountPresenter;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveUserView;
import com.livenation.mobile.android.platform.util.Logger;

public class FacebookLaunchActivity extends Activity implements AccountSaveAuthTokenView, AccountSaveUserView {
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private AccountPresenter accountPresenter = new AccountPresenter();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OpenRequest op = new Session.OpenRequest(this);
		op.setCallback(statusCallback);
		op.setPermissions(Arrays.asList("email"));
		
		Session session = new Builder(this).build();
        Session.setActiveSession(session);
        session.openForRead(op);
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}
	
	@Override
	public void onSaveAuthTokenSuccess() {
		finish();
	}
	
	@Override
	public void onSaveUserSuccess(User user) {
		finish();
	}
	
	@Override
	public void onSaveUserFailure() {
		Logger.log("Facebook", "Set user failed..");
		finish();
	}
	
	@Override
	public void onSaveAuthTokenFailure() {
		Logger.log("Facebook", "Set auth token failed..");
		finish();
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(final Session session, SessionState state,
				Exception exception) {
			if (session.isOpened()) {

				Request.newMeRequest(session, new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser graphUser, Response response) {
						if (null == graphUser) {
							FacebookLaunchActivity.this.onSaveUserFailure();
							return;
						}
						User user = FacebookSsoProvider.getAppUser(graphUser);
						
						Bundle userArgs = accountPresenter.getSetUserPresenter().getArgumentsBundle(user);
						Bundle authTokenArgs = accountPresenter.getSetAuthTokenPresenter().getFaceBookArgumentsBundle(session.getAccessToken());
						
						accountPresenter.getSetUserPresenter().initialize(FacebookLaunchActivity.this, userArgs, FacebookLaunchActivity.this);
						accountPresenter.getSetAuthTokenPresenter().initialize(FacebookLaunchActivity.this, authTokenArgs, FacebookLaunchActivity.this);
					}
				}).executeAsync();
			} 
		}
	}
	

}
