package com.livenation.mobile.android.na.presenters;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.na.presenters.views.AccountSaveAuthTokenView;
import com.livenation.mobile.android.na.presenters.views.AccountSignOutView;
import com.livenation.mobile.android.platform.sso.SsoManager;

import android.content.Context;
import android.os.Bundle;

public class AccountPresenters {
    private final SsoManager ssoManager;

    private final SaveAuthTokenPresenter saveAuthTokenPresenter;
    private final SignOutPresenter signOutPresenter;

    public AccountPresenters() {

        ssoManager = LiveNationApplication.getSsoManager();
        saveAuthTokenPresenter = new SaveAuthTokenPresenter(ssoManager);
        signOutPresenter = new SignOutPresenter(ssoManager);
    }

    public SaveAuthTokenPresenter getSetAuthToken() {
        return saveAuthTokenPresenter;
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
