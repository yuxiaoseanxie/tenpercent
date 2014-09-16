package com.livenation.mobile.android.na.providers;

import android.content.Context;

import com.livenation.mobile.android.na.preferences.AccessTokenPreferences;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

/**
 * Created by elodieferrais on 7/18/14.
 */
public class AccessTokenAppProvider extends com.livenation.mobile.android.platform.init.provider.AccessTokenProvider {
    private static AccessTokenPreferences accessTokenPreferences;

    public AccessTokenAppProvider(Context context) {
        accessTokenPreferences = new AccessTokenPreferences(context);
    }

    @Override
    public void getAccessToken(final BasicApiCallback<AccessToken> callback) {
        if (accessTokenPreferences.readAccessToken() != null) {
            callback.onResponse(accessTokenPreferences.readAccessToken());
            return;
        }
        super.getAccessToken(new BasicApiCallback<AccessToken>() {
            @Override
            public void onResponse(AccessToken response) {
                accessTokenPreferences.saveAccessToken(response);
                callback.onResponse(response);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        });
    }

    public void clear() {
        super.clearCache();
        accessTokenPreferences.clearAccessToken();
    }

    public void clearCache() {
        super.clearCache();
    }
}
