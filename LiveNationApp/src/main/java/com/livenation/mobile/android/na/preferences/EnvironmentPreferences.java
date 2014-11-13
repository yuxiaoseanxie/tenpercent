package com.livenation.mobile.android.na.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.BuildConfig;
import android.util.Log;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.utils.SerializableHelper;
import com.livenation.mobile.android.platform.init.Environment;

import java.io.IOException;

/**
 * Created by elodieferrais on 4/9/14.
 */
public class EnvironmentPreferences {
    SharedPreferences sharedPreferences;


    public EnvironmentPreferences(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(Constants.SharedPreferences.ENVIRONMENT, Context.MODE_PRIVATE);
    }

    public Environment getConfiguredEnvironment() {
        String environmentSerialize = sharedPreferences.getString(Constants.SharedPreferences.ENVIRONMENT, null);
        Environment env;
        if (BuildConfig.DEBUG) {
            Log.d("Elodie", "env debug");
            env = Environment.Staging;
        } else {
            Log.d("Elodie", "env prod");
            env = Environment.Production;
        }
        if (environmentSerialize != null && !environmentSerialize.isEmpty()) {
            try {
                env = (Environment) SerializableHelper.fromString(environmentSerialize);
            } catch (IOException e) {
                Log.d("Elodie", "IOException e");
                return Environment.Production;
            } catch (ClassNotFoundException e) {
                Log.d("Elodie", "ClassNotFoundException e");
                return Environment.Production;
            }
        }

        return env;
    }

    public void setConfiguredEnvironment(Environment environment) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            editor.putString(Constants.SharedPreferences.ENVIRONMENT, SerializableHelper.toString(environment)).apply();
        } catch (IOException e) {
            Log.e(EnvironmentPreferences.class.getSimpleName(), "Environment cannot be saved: " + environment.getHost());
        }
    }
}
