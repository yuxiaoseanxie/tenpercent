package com.livenation.mobile.android.na.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.apsalar.sdk.Apsalar;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;

/**
 * Created by elodieferrais on 7/18/14.
 */
public class AccessTokenPreferences {
    private SharedPreferences prefs;

    public AccessTokenPreferences(@NonNull Context context) {
        prefs = context.getSharedPreferences(Constants.SharedPreferences.API_NAME, Context.MODE_PRIVATE);

    }

    public void saveAccessToken(AccessToken accessToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.SharedPreferences.API_ACCESS_TOKEN, accessToken.getToken());
        editor.putString(Constants.SharedPreferences.API_ACCESS_TOKEN_TYPE, accessToken.getType());
        editor.commit();

        //Apsalar
        Apsalar.event(AnalyticConstants.APSALAR_LN_LOGIN, AnalyticConstants.TOKEN, accessToken.getToken(), AnalyticConstants.TOKEN_TYPE, accessToken.getType());
    }

    public AccessToken readAccessToken() {
        String token = prefs.getString(Constants.SharedPreferences.API_ACCESS_TOKEN, null);
        String type = prefs.getString(Constants.SharedPreferences.API_ACCESS_TOKEN_TYPE, null);
        if (token != null) {
            AccessToken accessToken = new AccessToken();
            accessToken.setType(type);
            accessToken.setToken(token);
            return accessToken;

        }
        return null;
    }

    public void clearAccessToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
