package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Intent;

public class DummySsoProvider implements UiApiSsoProvider {

    @Override
    public void openSession(boolean allowForeground,
                            OpenSessionCallback callback) {
        callback.onOpenSession(null);
    }

    @Override
    public void getUser(GetUserCallback callback) {
        callback.onGetUser(null);
    }

    @Override
    public void clearSession() {
    }

    @Override
    public String getTokenKey() {
        return null;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode,
                                 int resultCode, Intent data) {
    }

    public SsoManager.SSO_TYPE getId() {
        return SsoManager.SSO_TYPE.SSO_DUMMY;
    }

}
