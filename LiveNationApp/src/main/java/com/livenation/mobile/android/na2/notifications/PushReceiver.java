package com.livenation.mobile.android.na2.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.livenation.mobile.android.na2.notifications.ui.InboxActivity;
import com.livenation.mobile.android.na2.ui.HomeActivity;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;

/**
 * Created by km on 2/27/14.
 */
public class PushReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "Live Nation Notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(LOG_TAG, "Incoming notification intent: " + action);

        if(PushManager.ACTION_REGISTRATION_FINISHED.equals(action)) {
            registrationFinished(context, intent);
        } else if(PushManager.ACTION_NOTIFICATION_OPENED.equals(action)) {
            messageClicked(context, intent);
        }
    }

    private void messageClicked(Context context, Intent intent)
    {
        Log.i(LOG_TAG, "User clicked (" + intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0) +
                "): " + intent.getStringExtra(PushManager.EXTRA_ALERT));

        String messageId = intent.getStringExtra("_uamid");
        Intent inboxIntent = new Intent(context, InboxActivity.class);
        if(messageId != null) {
            inboxIntent.putExtra(InboxActivity.MESSAGE_ID_RECEIVED_KEY, messageId);
        } else {
            Log.i(LOG_TAG, "Message ID was missing.");
        }

        inboxIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(inboxIntent);
    }

    private void registrationFinished(Context context, Intent intent)
    {
        /* This is where platform registration will occur. -km */
        Log.i(LOG_TAG, "Registration finished with APID: " + intent.getStringExtra(PushManager.EXTRA_APID));
    }
}
