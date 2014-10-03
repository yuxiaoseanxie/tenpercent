package com.livenation.mobile.android.na.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.utils.SerializableHelper;
import com.livenation.mobile.android.platform.init.Environment;
import com.livenation.mobile.android.ticketing.Ticketing;

import java.io.IOException;

public class TicketingEnvironmentPreferences {
    SharedPreferences sharedPreferences;


    public TicketingEnvironmentPreferences(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(Constants.SharedPreferences.TICKETING_ENVIRONMENT, Context.MODE_PRIVATE);
    }

    public Ticketing.Environment getConfiguredEnvironment() {
        String environmentSerialize = sharedPreferences.getString(Constants.SharedPreferences.TICKETING_ENVIRONMENT, null);
        Ticketing.Environment env = Ticketing.Environment.PRODUCTION;
        if (environmentSerialize != null && !environmentSerialize.isEmpty()) {
            try {
                env = (Ticketing.Environment) SerializableHelper.fromString(environmentSerialize);
            } catch (IOException e) {
                return Ticketing.Environment.PRODUCTION;
            } catch (ClassNotFoundException e) {
                return Ticketing.Environment.PRODUCTION;
            }
        }

        return env;
    }

    public void setConfiguredEnvironment(Ticketing.Environment environment) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            editor.putString(Constants.SharedPreferences.TICKETING_ENVIRONMENT, SerializableHelper.toString(environment)).commit();
        } catch (IOException e) {
            Log.e(TicketingEnvironmentPreferences.class.getSimpleName(), "Environment cannot be saved: " + environment.name());
        }
    }
}