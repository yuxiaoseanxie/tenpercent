package com.livenation.mobile.android.na.notifications;

import android.util.Log;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.support.RegisterForNotificationsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;

public class NotificationsRegistrationManager implements Ticketing.PushTokenProvider {
    //region Lifecycle

    private static final NotificationsRegistrationManager instance = new NotificationsRegistrationManager();

    private NotificationsRegistrationManager() {
    }

    public static NotificationsRegistrationManager getInstance() {
        return instance;
    }

    //region Persistence

    private PreferencePersistence getPreferences() {
        return new PreferencePersistence("notifications", LiveNationApplication.get().getApplicationContext());
    }

    private void saveApid(String apid) {
        Log.i(getClass().getName(), "saved apid:" + apid);
        getPreferences().write(Constants.SharedPreferences.NOTIFICATIONS_SAVED_APID, apid);
    }

    private String getSavedApid() {
        return getPreferences().readString(Constants.SharedPreferences.NOTIFICATIONS_SAVED_APID);
    }

    //endregion


    //region Push Captcha

    @Override
    public String getPushToken() {
        if (BuildConfig.DEBUG)
            return null;
        else
            return PushManager.shared().getAPID();
    }

    //endregion


    //region Registration

    private boolean isHostSafe(String host) {
        return (!BuildConfig.DEBUG || (!"https://api.livenation.com".equals(host) && !"https://prod-faceoff.herokuapp.com".equals(host)));
    }

    public boolean shouldRegister() {
        String apid = PushManager.shared().getAPID();
        String userId = RichPushManager.shared().getRichPushUser().getId();
        return ((apid != null && !apid.equals(getSavedApid())) && userId != null);
    }

    public void register() {

        if (!isHostSafe(LiveNationLibrary.getHost())) {
            Log.e(getClass().getName(), "Ignoring unsafe host: " + LiveNationLibrary.getHost());
            return;
        }

        final String apid = PushManager.shared().getAPID();
        final String userId = RichPushManager.shared().getRichPushUser().getId();

        Log.i(getClass().getName(), "Registering with platform with apid: " + apid + ", UA user id: " + userId);

        RegisterForNotificationsParameters params = new RegisterForNotificationsParameters();
        params.setTokens(apid, userId);
        LiveNationApplication.getLiveNationProxy().registerForNotifications(params, new BasicApiCallback<Void>() {
            @Override
            public void onErrorResponse(LiveNationError error) {
                String errorMessage = (error != null) ? error.getMessage() : "unknown error";
                Log.e(getClass().getName(), "Could not register with platform: " + errorMessage, error);
            }

            @Override
            public void onResponse(Void response) {
                saveApid(apid);
                Log.i(getClass().getName(), "Completed platform registration");
            }
        });
    }

    //endregion
}
