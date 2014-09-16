package com.livenation.mobile.android.na.providers.sso;

import android.app.Activity;
import android.content.Context;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoManager;

/**
 * Created by elodieferrais on 9/16/14.
 */
public class SsoAppManager extends SsoManager {
    public SsoAppManager(Context context) {
        super(context);
    }

    @Override
    public void login(SSO_TYPE ssoType, boolean allowForeground, final SsoLoginCallback callback, Activity activity) {
        super.login(ssoType, allowForeground, new SsoLoginCallback() {
            @Override
            public void onLoginSucceed(String accessToken, User user) {
                LiveNationApplication.getAccessTokenProvider().clear();
                callback.onLoginSucceed(accessToken, user);
            }

            @Override
            public void onLoginFailed(LiveNationError error) {
                callback.onLoginFailed(error);
            }

            @Override
            public void onLoginCanceled() {
                callback.onLoginCanceled();
            }
        }, activity);
    }
}
