package com.livenation.mobile.android.na.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.livenation.mobile.android.na.helpers.ApiHelper;
import com.livenation.mobile.android.na.notifications.NotificationsRegistrationManager;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by elodieferrais on 4/17/14.
 */
public class UpdateReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ELODIE", "updateReceiver has been called");
        ApiHelper apiHelper =  LiveNationApplication.get().getApiHelper();
        apiHelper.setDependencyActivity(new Activity());
        if (!apiHelper.hasApi() && !apiHelper.isBuildingApi()) {
            LiveNationApplication.get().getApiHelper().buildDefaultApi();
        }

    }
}
