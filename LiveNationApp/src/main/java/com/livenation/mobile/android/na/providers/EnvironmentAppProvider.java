package com.livenation.mobile.android.na.providers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.livenation.mobile.android.na.preferences.EnvironmentPreferences;
import com.livenation.mobile.android.platform.init.Environment;

/**
 * Created by elodieferrais on 7/18/14.
 */
public class EnvironmentAppProvider implements com.livenation.mobile.android.platform.init.provider.EnvironmentProvider {
    private static EnvironmentPreferences environmentPreferences;
    private static Environment environment;

    public EnvironmentAppProvider(@NonNull Context context) {
        this.environmentPreferences = new EnvironmentPreferences(context);
    }

    @Override
    public Environment getEnvironment() {
        if (environment == null) {
            environment = environmentPreferences.getConfiguredEnvironment();
        }
        return environment;
    }

    private static void clear() {
        environment = null;
    }

    public static void setEnvironment(Environment environment) {
        environmentPreferences.setConfiguredEnvironment(environment);
        clear();
    }
}
