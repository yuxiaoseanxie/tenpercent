package com.livenation.mobile.android.na.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.livenation.mobile.android.na.app.Constants;

/**
 * Created by elodieferrais on 7/18/14.
 */
public class AccessTokenPreferences {
    private SharedPreferences prefs;

    public AccessTokenPreferences(@NonNull Context context) {
        prefs = context.getSharedPreferences(Constants.SharedPreferences.API_NAME, Context.MODE_PRIVATE);

    }

    public void saveAccessToken(String accessToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.SharedPreferences.API_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    public String readAccessToken() {
        return prefs.getString(Constants.SharedPreferences.API_ACCESS_TOKEN, null);
    }

    public void clearAccessToken() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
