package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Context;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

/**
 * Created by elodieferrais on 5/8/14.
 */
public class LoginHelper {
    private static SsoManager ssoManager = LiveNationApplication.getSsoManager();
    private  static Context applicationContext = LiveNationApplication.get().getApplicationContext();
    public static User getSavedUser() {
        return ssoManager.readUser(applicationContext);
    }

    public static SsoManager.AuthConfiguration getAuthConfiguration() {
        return ssoManager.getAuthConfiguration(applicationContext);
    }

    public static boolean isLoggin() {
        return ssoManager.readUser(applicationContext) != null;
    }

    public static void logout(Activity activity) {
        ssoManager.logout(activity);
        ssoManager.removeAuthConfiguration(applicationContext);
        ssoManager.removeUser(applicationContext);

        LiveNationApplication.get().getConfigManager().buildApi();
    }
}
