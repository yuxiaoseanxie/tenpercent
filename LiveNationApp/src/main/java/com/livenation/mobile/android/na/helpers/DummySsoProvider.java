package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Intent;

import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoLogoutCallback;

public class DummySsoProvider extends ApiSsoProvider {


    public DummySsoProvider() {
        super(null);
    }

    @Override
    public void login(boolean allowForeground, SsoLoginCallback callback) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data, SsoLoginCallback callback) {

    }

    @Override
    public Activity getActivity() {
        return null;
    }

    @Override
    public void login(boolean allowForeground) {
    }

    @Override
    public void getUser(ApiService.BasicApiCallback<User> callback) {
        callback.onResponse(null);
    }
}
