package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Intent;

import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoLogoutCallback;

public class DummySsoProvider extends ApiSsoProvider {


    @Override
    public void login(boolean allowForeground, SsoLoginCallback callback, Activity activity) {
        callback.onLoginSucceed(null, null);
    }

    @Override
    public void logout() {
    }

    @Override
    public void logout(SsoLogoutCallback callback) {
    }

    @Override
    public String getTokenKey() {
        return null;
    }

    public SsoManager.SSO_TYPE getId() {
        return SsoManager.SSO_TYPE.SSO_DUMMY;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data, SsoLoginCallback callback) {

    }

    @Override
    public void login(boolean allowForeground, Activity activity) {
    }
}
