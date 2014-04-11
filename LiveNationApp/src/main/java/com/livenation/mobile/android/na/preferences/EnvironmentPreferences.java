package com.livenation.mobile.android.na.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.utils.SerializableHelper;
import com.livenation.mobile.android.platform.init.Environment;
import com.livenation.mobile.android.platform.util.Logger;

import java.io.IOException;

/**
 * Created by elodieferrais on 4/9/14.
 */
public class EnvironmentPreferences {
    SharedPreferences sharedPreferences;


    public EnvironmentPreferences(Context context) {
        context.getApplicationContext().getSharedPreferences(Constants.SharedPreferences.ENVIRONMENT, Context.MODE_PRIVATE);
    }

    public Environment getConfiguredEnvironment() {
        String environmentSerialize = sharedPreferences.getString(Constants.SharedPreferences.ENVIRONMENT, null);
        Environment env;
        try {
            env = (Environment) SerializableHelper.fromString(environmentSerialize);
        } catch (IOException e) {
            return Environment.StagingDirect;
        } catch (ClassNotFoundException e) {
            return Environment.StagingDirect;
        }
        return env;
    }

    public void setConfiguredEnvironment(Environment environment) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            editor.putString(Constants.SharedPreferences.ENVIRONMENT, SerializableHelper.toString(environment)).commit();
        } catch (IOException e) {
            Logger.log(EnvironmentPreferences.class.getSimpleName(), "Environment cannot be saved: " + environment.getHost());
        }
    }
}
