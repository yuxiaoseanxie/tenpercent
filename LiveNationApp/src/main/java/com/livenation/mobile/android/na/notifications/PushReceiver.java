package com.livenation.mobile.android.na.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.ApiHelper;
import com.livenation.mobile.android.na.notifications.ui.InboxActivity;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters.RegisterForNotificationsParameters;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushUser;

public class PushReceiver extends BroadcastReceiver implements ApiServiceBinder {
    private static final String LOG_TAG = "Live Nation Notifications";
    private LiveNationApiService apiService;
    private boolean registeredBeforeApiAttached;

    public PushReceiver() {
        super();

        getApiHelper().persistentBindApi(this);
    }

    //region Api Binding

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        Log.i(LOG_TAG, "api service attached");
        this.apiService = apiService;
        if(registeredBeforeApiAttached) {
            Log.i(LOG_TAG, "calling register api after api service attached");
            registerWithApi();
        }
    }

    //endregion

    //region Reception

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(LOG_TAG, "Incoming notification intent: " + action);

        if (PushManager.ACTION_REGISTRATION_FINISHED.equals(action)) {
            registrationFinished(context, intent);
        } else if (PushManager.ACTION_NOTIFICATION_OPENED.equals(action)) {
            messageClicked(context, intent);
        }
    }

    private void messageClicked(Context context, Intent intent) {
        Log.i(LOG_TAG, "User clicked (" + intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0) +
                "): " + intent.getExtras());

        String messageId = intent.getStringExtra(Constants.Notifications.EXTRA_RICH_MESSAGE_ID);
        Intent outgoingIntent = new Intent(context, InboxActivity.class);
        outgoingIntent.putExtra(InboxActivity.MESSAGE_ID_RECEIVED_KEY, messageId);
        outgoingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(outgoingIntent);
    }

    private void registrationFinished(Context context, Intent intent) {
        String apid = intent.getStringExtra(PushManager.EXTRA_APID);
        Log.i(LOG_TAG, "Registration finished with APID: " + apid);

        if(apiService == null) {
            registeredBeforeApiAttached = true;
        } else {
            registerWithApi();
        }
    }

    //endregion


    //region Registering With Platform

    private void registerWithApi() {
        String apid = PushManager.shared().getAPID();
        String userId = RichPushManager.shared().getRichPushUser().getId();

        Log.i(LOG_TAG, "Registering with platform with apid: " + apid + ", UA user id: " + userId);

        RegisterForNotificationsParameters params = new RegisterForNotificationsParameters();
        params.setDeviceId(apiService.getApiConfig().getDeviceId());
        params.setTokens(apid, userId);
        apiService.registerForNotifications(params, new ApiService.BasicApiCallback<Void>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Could not register with platform: " + error);
            }

            @Override
            public void onResponse(Void response) {
                Log.i(LOG_TAG, "Completed platform registration");
            }
        });
    }

    private ApiHelper getApiHelper() {
        return LiveNationApplication.get().getApiHelper();
    }

    //endregion
}
