package com.livenation.mobile.android.na.helpers;

import android.content.Context;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.sso.SsoUpdatedUserCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoLogoutCallback;

/**
 * Created by elodieferrais on 5/8/14.
 */
public class LoginHelper {
    private static int USER_UPDATE_PERIOD = 1000 * 60 * 5;
    private static SsoManager ssoManager = LiveNationApplication.getSsoManager();
    private static Context applicationContext = LiveNationApplication.get().getApplicationContext();

    public static User getSavedUser() {
        return ssoManager.readUser(applicationContext);
    }

    public static void getUpdatedUser(final SsoUpdatedUserCallback callback) {
        SsoManager.SSO_TYPE ssotype = getAuthConfiguration().getSsoProviderId();
        if (ssotype.equals(SsoManager.SSO_TYPE.SSO_DUMMY)) {
            if (callback != null) {
                callback.onResponse(false, null, null);
            }
            return;
        } else if (getAuthConfiguration().getTimestamp() != null && Math.abs(getAuthConfiguration().getTimestamp() - System.currentTimeMillis()) < USER_UPDATE_PERIOD) {
            callback.onResponse(false, getAuthConfiguration().getAccessToken(), getSavedUser());
        } else {
            ssoManager.login(ssotype, applicationContext, false, new SsoLoginCallback() {
                @Override
                public void onLoginSucceed(String accessToken, User user) {
                    if (callback != null) {
                        boolean hasTokenChanged = !accessToken.equals(getAuthConfiguration().getAccessToken());
                        if (hasTokenChanged) {
                            LiveNationApplication.get().getConfigManager().clearAccessToken();
                            LiveNationApplication.get().getConfigManager().buildApi();
                        }
                        callback.onResponse(hasTokenChanged, getAuthConfiguration().getAccessToken(), user);
                    }
                }

                @Override
                public void onLoginFailed(final LiveNationError error) {
                    ssoManager.logout(applicationContext, new SsoLogoutCallback() {
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
            });
        }
    }

    public static SsoManager.AuthConfiguration getAuthConfiguration() {
        return ssoManager.getAuthConfiguration(applicationContext);
    }

    public static boolean isLogout() {
        return ssoManager.readUser(applicationContext) == null;
    }

    public static boolean isLogin() {
        return ssoManager.readUser(applicationContext) != null;
    }


    public static void logout(Context context, SsoLogoutCallback callback) {
        ssoManager.logout(context, callback);
    }

    public static boolean isUsingFacebook(Context context) {
        SsoManager.AuthConfiguration auth = LiveNationApplication.get().getSsoManager().getAuthConfiguration(context);
        return (auth != null && auth.getSsoProviderId() == SsoManager.SSO_TYPE.SSO_FACEBOOK);
    }


    public static boolean isUsingGoogle(Context context) {
        SsoManager.AuthConfiguration auth = LiveNationApplication.get().getSsoManager().getAuthConfiguration(context);
        return (auth != null && auth.getSsoProviderId() == SsoManager.SSO_TYPE.SSO_GOOGLE);
    }
}
