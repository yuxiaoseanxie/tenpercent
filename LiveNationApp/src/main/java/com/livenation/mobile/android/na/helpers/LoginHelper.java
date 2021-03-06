package com.livenation.mobile.android.na.helpers;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.providers.sso.SsoProviderPersistence;
import com.livenation.mobile.android.na.providers.sso.SsoUpdatedUserCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.error.ErrorDictionary;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoLogoutCallback;
import com.livenation.mobile.android.platform.sso.SsoManager;

import android.app.Activity;
import android.content.Context;

/**
 * Created by elodieferrais on 5/8/14.
 */
public class LoginHelper {
    private static int USER_UPDATE_PERIOD = 1000 * 60 * 5;
    private static SsoProviderPersistence ssoProviderPersistence = LiveNationApplication.getSsoProviderPersistence();
    private static SsoManager ssoManager = LiveNationApplication.getSsoManager();

    public static User getSavedUser() {
        return ssoProviderPersistence.readUser();
    }

    public static void getUpdatedUser(final SsoUpdatedUserCallback callback, Activity activity) {
        if (activity == null) {
            if (LoginHelper.isLogin()) {
                callback.onResponse(false, getAuthConfiguration().getAccessToken(), LoginHelper.getSavedUser());
            } else {
                callback.onErrorResponse(new LiveNationError(ErrorDictionary.ERROR_CODE_SSO_FACEBOOK_LOGIN_ACTIVITY_NULL));
            }
            return;
        }

        SsoManager.AuthConfiguration authConfiguration = getAuthConfiguration();
        if (authConfiguration == null) {
            if (callback != null) {
                callback.onResponse(false, null, null);
            }
            return;
        } else if (authConfiguration.getTimestamp() != null && Math.abs(authConfiguration.getTimestamp() - System.currentTimeMillis()) < USER_UPDATE_PERIOD) {
            callback.onResponse(false, getAuthConfiguration().getAccessToken(), getSavedUser());
        } else {
            login(authConfiguration.getSsoProviderId(), false, callback, activity);
        }
    }

    public static SsoManager.AuthConfiguration getAuthConfiguration() {
        return ssoProviderPersistence.getAuthConfiguration();
    }

    public static void login(final SsoManager.SSO_TYPE ssoType, boolean allowForeground, final SsoUpdatedUserCallback callback, Activity activity) {
        ssoManager.login(ssoType, allowForeground, new SsoLoginCallback() {
            @Override
            public void onLoginSucceed(String ssoAccessToken, User user) {
                if (callback != null) {
                    boolean hasTokenChanged = !ssoAccessToken.equals(getAuthConfiguration().getAccessToken());
                    if (hasTokenChanged) {
                        LiveNationApplication.getAccessTokenProvider().clearCache();
                    }
                    callback.onResponse(hasTokenChanged, getAuthConfiguration().getAccessToken(), user);
                }
            }

            @Override
            public void onLoginFailed(final LiveNationError error) {
                ssoManager.logout(new SsoLogoutCallback() {
                    @Override
                    public void onLogoutSucceed() {
                        if (callback != null) {
                            callback.onErrorResponse(error);
                        }
                    }

                    @Override
                    public void onLogoutFailed(LiveNationError error) {
                        if (callback != null) {
                            callback.onErrorResponse(error);
                        }
                    }
                });
            }

            @Override
            public void onLoginCanceled() {
                //Should never happen in this case
            }
        }, activity);
    }

    public static boolean isLogout() {
        return ssoProviderPersistence.readUser() == null;
    }

    public static boolean isLogin() {
        return ssoProviderPersistence.readUser() != null;
    }


    public static void logout(SsoLogoutCallback callback) {
        ssoManager.logout(callback);
    }

    public static boolean isUsingFacebook(Context context) {
        SsoManager.AuthConfiguration auth = ssoProviderPersistence.getAuthConfiguration();
        return (auth != null && auth.getSsoProviderId() == SsoManager.SSO_TYPE.SSO_FACEBOOK);
    }


    public static boolean isUsingGoogle(Context context) {
        SsoManager.AuthConfiguration auth = ssoProviderPersistence.getAuthConfiguration();
        return (auth != null && auth.getSsoProviderId() == SsoManager.SSO_TYPE.SSO_GOOGLE);
    }
}
