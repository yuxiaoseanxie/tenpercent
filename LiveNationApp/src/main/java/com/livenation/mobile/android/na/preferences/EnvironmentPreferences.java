package com.livenation.mobile.android.na.preferences;

import android.content.Context;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.PersistenceProvider;
import com.livenation.mobile.android.na.helpers.PreferencePersistence;

/**
 * Created by elodieferrais on 4/9/14.
 */
public class EnvironmentPreferences {
    public static Constants.Environment getConfiguredEnvironment(Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence(Constants.SharedPreferences.ENVIRONMENT);
        String environmentKey = prefs.read(Constants.SharedPreferences.ENVIRONMENT, context);

        try {
            return Constants.Environment.valueOf(environmentKey);
        } catch (Exception e) {
            return Constants.Environment.StagingDirect;
        }
    }

    public static void setConfiguredEnvironment(Constants.Environment environment, Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence(Constants.SharedPreferences.ENVIRONMENT);
        prefs.write(Constants.SharedPreferences.ENVIRONMENT, environment.toString(), context);
    }
}
