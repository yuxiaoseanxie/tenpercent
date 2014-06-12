package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.os.Bundle;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveUserView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

public class AccountPresenters {
    private final SsoManager ssoManager;

    private final SaveAuthTokenPresenter saveAuthTokenPresenter;
    private final SaveUserPresenter saveUserPresenter;
    private final SignOutPresenter signOutPresenter;

    public AccountPresenters() {

        ssoManager = LiveNationApplication.getSsoManager();
        saveAuthTokenPresenter = new SaveAuthTokenPresenter(ssoManager);
        saveUserPresenter = new SaveUserPresenter(ssoManager);
        signOutPresenter = new SignOutPresenter(ssoManager);
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
                view.onSaveAuthTokenSuccess();
                return;
            }

            view.onSaveAuthTokenSuccess();
        }

    }

    public static class SaveUserPresenter extends BaseSsoPresenter<AccountSaveUserView> implements Presenter<AccountSaveUserView> {
        private static String ARG_USER = "sso_user";

        public SaveUserPresenter(SsoManager ssoManager) {
            super(ssoManager);
        }

        @Override
        public void initialize(Context context, Bundle args, final AccountSaveUserView view) {
            ssoManager.getConfiguredSsoProvider(context).getUser(new ApiService.BasicApiCallback<User>() {
                @Override
                public void onResponse(User user) {
                    view.onSaveUserSuccess(user);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {

                }
            });
        }
    }

    public static class SignOutPresenter extends BaseSsoPresenter<AccountSignOutView> implements Presenter<AccountSignOutView> {

        public SignOutPresenter(SsoManager ssoManager) {
            super(ssoManager);
        }

        @Override
        public void initialize(Context context, Bundle args, AccountSignOutView view) {
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
