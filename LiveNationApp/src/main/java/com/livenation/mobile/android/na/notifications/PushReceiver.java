package com.livenation.mobile.android.na.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.notifications.ui.InboxActivity;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.urbanairship.push.PushManager;

import com.segment.android.Analytics;
import com.segment.android.models.Props;

public class PushReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "Live Nation Notifications";

    //region Reception

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(LOG_TAG, "Incoming notification intent: " + action);

        if (PushManager.ACTION_REGISTRATION_FINISHED.equals(action)) {
            registrationFinished(context, intent);
        } else if (PushManager.ACTION_NOTIFICATION_OPENED.equals(action)) {
            messageClicked(context, intent);
        } else if (PushManager.ACTION_PUSH_RECEIVED.equals(action)) {
            pushReceived(context, intent);
        }
    }

    private void pushReceived(Context context, Intent intent) {
        Log.i(LOG_TAG, "Push received: " + TextUtils.join(", ", intent.getExtras().keySet()));
        String type = intent.getStringExtra(Constants.Notifications.EXTRA_TYPE);
        if (!BuildConfig.DEBUG && Constants.Notifications.TYPE_PUSH_CAPTCHA.equals(type)) {
            String pushCaptchaPayload = intent.getStringExtra(Constants.Notifications.EXTRA_PUSH_CAPTCHA_PAYLOAD);
            Ticketing.setPushCaptchaPayload(pushCaptchaPayload);
        }

        String messageValue = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_VALUE);
        String messageId = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_ID);
        Props props = new Props();
        props.put(AnalyticConstants.MESSAGE_VALUE, messageValue);
        props.put(AnalyticConstants.MESSAGE_ID, messageId);
        LiveNationAnalytics.track(AnalyticConstants.PUSH_NOTIFICATION_RECEIVE, AnalyticsCategory.PUSHNOTIFICATION, props);
    }

    private void messageClicked(Context context, Intent intent) {
        Log.i(LOG_TAG, "User clicked (" + intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0) +
                "): " + intent.getExtras());

        String messageId = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_ID);
        Intent outgoingIntent = new Intent(context, InboxActivity.class);
        outgoingIntent.putExtra(InboxActivity.MESSAGE_ID_RECEIVED_KEY, messageId);
        outgoingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(outgoingIntent);

        String messageValue = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_VALUE);
        Props props = new Props();
        props.put(AnalyticConstants.MESSAGE_VALUE, messageValue);
        props.put(AnalyticConstants.MESSAGE_ID, messageId);
        LiveNationAnalytics.track(AnalyticConstants.PUSH_NOTIFICATION_TAP, AnalyticsCategory.PUSHNOTIFICATION, props);

    }

    private void registrationFinished(Context context, Intent intent) {
        String apid = intent.getStringExtra(PushManager.EXTRA_APID);
        Log.i(LOG_TAG, "Registration finished with APID: " + apid);

        NotificationsRegistrationManager.getInstance().register();
    }

    //endregion
}
