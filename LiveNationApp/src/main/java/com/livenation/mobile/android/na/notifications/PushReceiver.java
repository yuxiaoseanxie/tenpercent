package com.livenation.mobile.android.na.notifications;

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
import com.segment.android.models.Props;
import com.urbanairship.push.BaseIntentReceiver;
import com.urbanairship.push.PushMessage;

public class PushReceiver extends BaseIntentReceiver {
    private static final String LOG_TAG = "Live Nation Notifications";

    @Override
    protected void onChannelRegistrationSucceeded(Context context, String s) {
        //getResultData()
        //getResultExtras(true)
        registrationFinished(s);
    }

    @Override
    protected void onChannelRegistrationFailed(Context context) {
        registrationFailed("a");
    }

    @Override
    protected void onPushReceived(Context context, PushMessage pushMessage, int i) {
        Log.i(LOG_TAG, "Push received: " + TextUtils.join(", ", pushMessage.getPushBundle().keySet()));

        //String type = intent.getStringExtra(Constants.Notifications.EXTRA_TYPE);
        String type = pushMessage.getInteractiveNotificationType();

        if (!BuildConfig.DEBUG && Constants.Notifications.TYPE_PUSH_CAPTCHA.equals(type)) {
            //String pushCaptchaPayload = intent.getStringExtra(Constants.Notifications.EXTRA_PUSH_CAPTCHA_PAYLOAD);
            String pushCaptchaPayload = pushMessage.getActionsPayload();

            Ticketing.setPushCaptchaPayload(pushCaptchaPayload);
        }

        //String messageValue = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_VALUE);
        //String messageId = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_ID);

        String messageValue = pushMessage.getAlert();
        String messageId = pushMessage.getRichPushMessageId();


        Props props = new Props();
        props.put(AnalyticConstants.MESSAGE_VALUE, messageValue);
        props.put(AnalyticConstants.MESSAGE_ID, messageId);
        LiveNationAnalytics.track(AnalyticConstants.PUSH_NOTIFICATION_RECEIVE, AnalyticsCategory.PUSHNOTIFICATION, props);
    }

    @Override
    protected void onBackgroundPushReceived(Context context, PushMessage pushMessage) {
    }

    @Override
    protected boolean onNotificationOpened(Context context, PushMessage pushMessage, int i) {
        messageClicked(context, pushMessage);
        return false;
    }

    @Override
    protected boolean onNotificationActionOpened(Context context, PushMessage pushMessage, int i, String s, boolean b) {
        return false;
    }

    private void messageClicked(Context context, PushMessage message) {
        Log.i(LOG_TAG, "User clicked (" + message.getRichPushMessageId() +
                "): " + message.getPushBundle().toString());

        String messageId = message.getRichPushMessageId();
        Intent outgoingIntent = new Intent(context, InboxActivity.class);
        outgoingIntent.putExtra(InboxActivity.MESSAGE_ID_RECEIVED_KEY, messageId);
        outgoingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(outgoingIntent);

        //String messageValue = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_VALUE);
        String messageValue = message.getAlert();

        Props props = new Props();
        props.put(AnalyticConstants.MESSAGE_VALUE, messageValue);
        props.put(AnalyticConstants.MESSAGE_ID, messageId);
        LiveNationAnalytics.track(AnalyticConstants.PUSH_NOTIFICATION_TAP, AnalyticsCategory.PUSHNOTIFICATION, props);

    }

    private void registrationFinished(String string) {
        Log.i(LOG_TAG, "Registration finished with ChannelId: " + string);
        NotificationsRegistrationManager.getInstance().register();
    }

    private void registrationFailed(String string) {
        Log.e(LOG_TAG, "Registration failed with ChannelId: " + string);
    }

    //endregion
}
