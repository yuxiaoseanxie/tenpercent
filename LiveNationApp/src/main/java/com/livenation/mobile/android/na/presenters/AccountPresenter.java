package com.livenation.mobile.android.na.presenters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveUserView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public class AccountPresenter implements Presenter<AccountUserView> {
	private final SsoProfilePresenter ssoProfilePresenter;
	private final SsoManager ssoManager;
	private final SaveAuthTokenPresenter saveAuthTokenPresenter;
	private final SaveUserPresenter saveUserPresenter;
	private final SignOutPresenter signOutPresenter;

	private static final String INTENT_DATA_KEY = EventsPresenter.class.getName();
	
	public AccountPresenter(SsoManager ssoManager) {
		this.ssoManager = ssoManager;
		
		ssoProfilePresenter  = new SsoProfilePresenter();
		
		saveAuthTokenPresenter = new SaveAuthTokenPresenter(ssoManager);
		saveUserPresenter = new SaveUserPresenter(ssoManager);
		signOutPresenter = new SignOutPresenter(ssoManager);
	}
	
	@Override
	public void initialize(Context context, Bundle args, AccountUserView view) {
		User user = ssoManager.readUser(context);
		view.setUser(user);
	}

	public Presenter<AccountUserView> getProfilePresenter() {
		return ssoProfilePresenter;
	}

	public SaveAuthTokenPresenter getSetAuthTokenPresenter() {
		return saveAuthTokenPresenter;
	}
	
	public SaveUserPresenter getSetUserPresenter() {
		return saveUserPresenter;
	}
	
	public SignOutPresenter getSignOutPresenter() {
		return signOutPresenter;
	}
	
	public static Bundle getArgumentsBundle(User user) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(INTENT_DATA_KEY, user);
		return bundle;
	}
	
	public static class SaveAuthTokenPresenter extends BaseSsoPresenter implements Presenter<AccountSaveAuthTokenView> {
		private static String ARG_ACCESS_TOKEN_KEY = "access_token";
		private static String ARG_SSO_PROVIDER_ID = "sso_id";
				
		public SaveAuthTokenPresenter(SsoManager ssoManager) {
			super(ssoManager);
		}		
		
		@Override
		public void initialize(Context context, Bundle args,
				AccountSaveAuthTokenView view) {
			
			if (null == args) {
				ssoManager.removeAuthConfiguration(context);
				return;			
			}
			
			Integer providerId = args.getInt(ARG_SSO_PROVIDER_ID);
			String accessToken = args.getString(ARG_ACCESS_TOKEN_KEY);
			
			ssoManager.saveAuthConfiguration(providerId, accessToken, context);
			view.onSaveAuthTokenSuccess();
		}
	
		public Bundle getArgumentsBundle(int providerId, String accessToken) {
			Bundle bundle = new Bundle();
			
			bundle.putString(ARG_ACCESS_TOKEN_KEY, accessToken);
			bundle.putInt(ARG_SSO_PROVIDER_ID, providerId);
			
			return bundle;
		}
	}
	
	public static class SaveUserPresenter extends BaseSsoPresenter implements Presenter<AccountSaveUserView> {
		private static String ARG_USER = "sso_user";
		
		public SaveUserPresenter(SsoManager ssoManager) {
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
			ssoManager.logout((Activity) context);
			ssoManager.removeAuthConfiguration(context);
			ssoManager.removeUser(context);
			view.onSignOut();
		}
		
	}
	
	public static class SsoProfilePresenter implements Presenter<AccountUserView> {
		
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
