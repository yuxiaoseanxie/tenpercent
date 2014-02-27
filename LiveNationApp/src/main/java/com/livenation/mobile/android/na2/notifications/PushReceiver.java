package com.livenation.mobile.android.na2.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.livenation.mobile.android.na2.ui.HomeActivity;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

/**
 * Created by km on 2/27/14.
 */
public class PushReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "Live Nation Notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(PushManager.ACTION_REGISTRATION_FINISHED.equals(action)) {
            Log.i(LOG_TAG, "Registration finished with APID: " + intent.getStringExtra(PushManager.EXTRA_APID));
        } else if(PushManager.ACTION_NOTIFICATION_OPENED.equals(action)) {
            Log.i(LOG_TAG, "User clicked (" + intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0) +
                    "): " + intent.getStringExtra(PushManager.EXTRA_ALERT));

            Context applicationContext = UAirship.shared().getApplicationContext();
            Intent launchHomeActivity = new Intent(Intent.ACTION_MAIN);
            launchHomeActivity.setClass(applicationContext, HomeActivity.class);
            launchHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            applicationContext.startActivity(launchHomeActivity);
        } else if(PushManager.ACTION_PUSH_RECEIVED.equals(action)) {
            Log.i(LOG_TAG, "Received push (" + intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0) +
                    "): " + intent.getStringExtra(PushManager.EXTRA_ALERT));
        }
    }
}
