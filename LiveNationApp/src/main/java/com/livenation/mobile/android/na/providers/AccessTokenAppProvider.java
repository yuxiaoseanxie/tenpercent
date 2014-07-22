package com.livenation.mobile.android.na.providers;

import android.content.Context;

import com.livenation.mobile.android.na.preferences.AccessTokenPreferences;
import com.livenation.mobile.android.na.preferences.EnvironmentPreferences;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;

/**
 * Created by elodieferrais on 7/18/14.
 */
public class AccessTokenAppProvider extends com.livenation.mobile.android.platform.init.provider.AccessTokenProvider{
    private static AccessTokenPreferences accessTokenPreferences;

    public AccessTokenAppProvider(Context context) {
        accessTokenPreferences = new AccessTokenPreferences(context);
    }

    @Override
    public void clear() {
        super.clear();
        accessTokenPreferences.clearAccessToken();
    }
}
