package com.livenation.mobile.android.na.presenters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveUserView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public class AccountPresenters {
	private final SsoManager ssoManager;

    private final GetUserPresenter getUserPresenter;
    private final SaveAuthTokenPresenter saveAuthTokenPresenter;
	private final SaveUserPresenter saveUserPresenter;
	private final SignOutPresenter signOutPresenter;

	public AccountPresenters(SsoManager ssoManager) {
		this.ssoManager = ssoManager;
		
        getUserPresenter = new GetUserPresenter(ssoManager);
		saveAuthTokenPresenter = new SaveAuthTokenPresenter(ssoManager);
		saveUserPresenter = new SaveUserPresenter(ssoManager);
		signOutPresenter = new SignOutPresenter(ssoManager);
	}

    public GetUserPresenter getGetUser() {
        return getUserPresenter;
    }

	public SaveAuthTokenPresenter getSetAuthToken() {
		return saveAuthTokenPresenter;
	}
	
	public SaveUserPresenter getSetUser() {
		return saveUserPresenter;
	}
	
	public SignOutPresenter getSignOut() {
		return signOutPresenter;
	}

    public static class GetUserPresenter implements Presenter<AccountUserView> {
        private final SsoManager ssoManager;

        private GetUserPresenter(SsoManager ssoManager) {
            this.ssoManager = ssoManager;
        }

        @Override
        public void initialize(Context context, Bundle args, AccountUserView view) {
            User user = ssoManager.readUser(context);
            view.setUser(user);
        }

        @Override
        public void cancel(AccountUserView view) {
            //do nothing, no state
        }
    }
	
	public static class SaveAuthTokenPresenter extends BaseSsoPresenter<AccountSaveAuthTokenView> implements Presenter<AccountSaveAuthTokenView> {
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
	
		public Bundle getArguments(int providerId, String accessToken) {
			Bundle bundle = new Bundle();
			
			bundle.putString(ARG_ACCESS_TOKEN_KEY, accessToken);
			bundle.putInt(ARG_SSO_PROVIDER_ID, providerId);
			
			return bundle;
		}
	}
	
	public static class SaveUserPresenter extends BaseSsoPresenter<AccountSaveUserView> implements Presenter<AccountSaveUserView> {
		private static String ARG_USER = "sso_user";
		
		public SaveUserPresenter(SsoManager ssoManager) {
			super(ssoManager);
		}

		@Override
		public void initialize(Context context, Bundle args, AccountSaveUserView view) {
			User user = (User) args.getSerializable(ARG_USER);
			ssoManager.saveUser(user, context);
			view.onSaveUserSuccess(user);
		}

        public Bundle getArguments(User user) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(ARG_USER, user);
            return bundle;
        }
	}
	
	public static class SignOutPresenter extends BaseSsoPresenter<AccountSignOutView> implements Presenter<AccountSignOutView> {
		
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
	
	private static abstract class BaseSsoPresenter<T extends PresenterView> implements Presenter<T> {
		protected final SsoManager ssoManager;
		
		public BaseSsoPresenter(SsoManager ssoManager) {
			this.ssoManager = ssoManager;
		}

        @Override
        public void cancel(T view) {
            //no state,
        }
    }


}