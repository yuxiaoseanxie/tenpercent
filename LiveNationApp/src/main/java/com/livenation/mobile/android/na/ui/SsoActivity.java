package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.sso.SsoLoginCallback;
import com.livenation.mobile.android.platform.sso.SsoManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SsoActivity extends LiveNationFragmentActivity implements SsoLoginCallback {
    public static final String ARG_PROVIDER_ID = "provider_id";
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final int RESULT_CANCELED = Activity.RESULT_CANCELED;
    public static final int RESULT_ERROR = 111;
    public SsoManager.SSO_TYPE providerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(ARG_PROVIDER_ID)) {
            providerId = SsoManager.SSO_TYPE.valueOf(getIntent().getExtras().getString(ARG_PROVIDER_ID));
        }

        if (null == providerId) {
            throw new IllegalArgumentException("Which SSO do you want me to sign in?!");
        }

        LiveNationApplication.get().getSsoManager().login(providerId, true, this, this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (providerId != null) {
            LiveNationApplication.getSsoManager().getSsoProvider(providerId)
                    .onActivityResult(this, requestCode, resultCode, data, this);
        }
    }

    @Override
    public void onLoginSucceed(String ssoAccessToken, User user) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onLoginFailed(LiveNationError error) {
        setResult(RESULT_ERROR);
        finish();
    }

    @Override
    public void onLoginCanceled() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
