package com.livenation.mobile.android.na.helpers;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

/**
 * Created by elodieferrais on 5/8/14.
 */
public class LoginHelper {
    public static User getSavedUser() {
        return LiveNationApplication.getSsoManager().readUser(LiveNationApplication.get().getApplicationContext());
    }

    public static SsoManager.AuthConfiguration getAuthConfiguration() {
        return LiveNationApplication.getSsoManager().getAuthConfiguration(LiveNationApplication.get().getApplicationContext());
    }

    public static boolean isLoggin() {
        return LiveNationApplication.getSsoManager().readUser(LiveNationApplication.get().getApplicationContext()) != null;
    }
}
