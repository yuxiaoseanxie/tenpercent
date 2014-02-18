package com.livenation.mobile.android.na.presenters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.models.User;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveUserView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;

public class AccountPresenter implements Presenter<AccountUserView> {
	private final FacebookProfilePresenter facebookProfilePresenter;
	private final SsoManager ssoManager;
	private final SetAuthTokenPresenter setAuthTokenPresenter;
	private final SetUserPresenter setUserPresenter;
	private final SignOutPresenter signOutPresenter;

	private static final String INTENT_DATA_KEY = EventsPresenter.class.getName();
	
	public AccountPresenter() {
		ssoManager = new SsoManager();
		facebookProfilePresenter  = new FacebookProfilePresenter();
		
		setAuthTokenPresenter = new SetAuthTokenPresenter(ssoManager);
		setUserPresenter = new SetUserPresenter(ssoManager);
		signOutPresenter = new SignOutPresenter(ssoManager);
	}
	
	@Override
	public void initialize(Context context, Bundle args, AccountUserView view) {
		User user = ssoManager.readUser(context);
		view.setUser(user);
	}

	public Presenter<AccountUserView> getActiveProfilePresenter() {
		return facebookProfilePresenter;
	}
		
	public Intent getFacebookSigninIntent(Activity activity) {
		return ssoManager.getSignInIntent(SsoManager.SSO_FACEBOOK, activity);
	}
	
	public FacebookProfilePresenter getFacebookProfilePresenter() {
		return facebookProfilePresenter;
	}
	
	public SetAuthTokenPresenter getSetAuthTokenPresenter() {
		return setAuthTokenPresenter;
	}
	
	public SetUserPresenter getSetUserPresenter() {
		return setUserPresenter;
	}
	
	public SignOutPresenter getSignOutPresenter() {
		return signOutPresenter;
	}
	
	public static Bundle getArgumentsBundle(User user) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(INTENT_DATA_KEY, user);
		return bundle;
	}
	
	public static class SetAuthTokenPresenter extends BaseSsoPresenter implements Presenter<AccountSaveAuthTokenView> {
		private static String ARG_AUTH_TOKEN_KEY = "auth_token";
		private static String ARG_SSO_PROVIDER_ID = "sso_id";
				
		public SetAuthTokenPresenter(SsoManager ssoManager) {
			super(ssoManager);
		}		
		
		@Override
		public void initialize(Context context, Bundle args,
				AccountSaveAuthTokenView view) {
			Integer providerId = args.getInt(ARG_SSO_PROVIDER_ID);
			String authToken = args.getString(ARG_AUTH_TOKEN_KEY);
			
			if (null == authToken) {
				ssoManager.removeAuthToken(context);
				return;
			}
			
			ssoManager.saveAuthToken(providerId, authToken, context);
			view.onSaveAuthTokenSuccess();
		}

		public Bundle getFaceBookArgumentsBundle(String accessToken) {
			Bundle bundle = new Bundle();
			
			bundle.putString(ARG_AUTH_TOKEN_KEY, accessToken);
			bundle.putInt(ARG_SSO_PROVIDER_ID, SsoManager.SSO_FACEBOOK);
			
			return bundle;
		}
	}
	
	public static class SetUserPresenter extends BaseSsoPresenter implements Presenter<AccountSaveUserView> {
		private static String ARG_USER = "sso_user";
		
		public SetUserPresenter(SsoManager ssoManager) {
			super(ssoManager);
		}
		
		public Bundle getArgumentsBundle(User user) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(ARG_USER, user);
			return bundle;
		}

		@Override
		public void initialize(Context context, Bundle args, AccountSaveUserView view) {
			User user = (User) args.getSerializable(ARG_USER);
			ssoManager.saveUser(user, context);
			view.onSaveUserSuccess(user);
		}
	}
	
	public static class SignOutPresenter extends BaseSsoPresenter implements Presenter<AccountSignOutView> {
		
		public SignOutPresenter(SsoManager ssoManager) {
			super(ssoManager);
		}
		
		@Override
		public void initialize(Context context, Bundle args, AccountSignOutView view) {
			ssoManager.logout(context);
			ssoManager.removeAuthToken(context);
			ssoManager.removeUser(context);
			view.onSignOut();
		}
		
	}
	
	public static class FacebookProfilePresenter implements Presenter<AccountUserView> {
		
		@Override
		public void initialize(Context context, Bundle args,
				AccountUserView view) {
			if (args.containsKey(INTENT_DATA_KEY)) {
				User user = (User) args.getSerializable(INTENT_DATA_KEY);
				view.setUser(user);
			}
		}
		
	}
	
	private static abstract class BaseSsoPresenter {
		protected final SsoManager ssoManager;
		
		public BaseSsoPresenter(SsoManager ssoManager) {
			this.ssoManager = ssoManager;
		}
	}

}
