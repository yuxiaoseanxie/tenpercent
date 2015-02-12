package com.livenation.mobile.android.na.providers;

import com.livenation.mobile.android.na.preferences.EnvironmentPreferences;
import com.livenation.mobile.android.platform.init.Environment;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by elodieferrais on 7/18/14.
 */
public class EnvironmentAppProvider implements com.livenation.mobile.android.platform.init.provider.EnvironmentProvider {
    private static EnvironmentPreferences environmentPreferences;
    private static Environment environment;

    public EnvironmentAppProvider(@NonNull Context context) {
        this.environmentPreferences = new EnvironmentPreferences(context);
    }

    private static void clear() {
        environment = null;
    }

    @Override
    public Environment getEnvironment() {
        if (environment == null) {
            environment = environmentPreferences.getConfiguredEnvironment();
        }
        return environment;
    }

    public static void setEnvironment(Environment environment) {
        environmentPreferences.setConfiguredEnvironment(environment);
        clear();
    }
}
