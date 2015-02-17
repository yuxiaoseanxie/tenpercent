package com.livenation.mobile.android.na.notifications;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.support.RegisterForNotificationsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;

import android.util.Log;

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

    private void saveChannelId(String channelId) {
        Log.i(getClass().getName(), "saved channelId:" + channelId);
        getPreferences().write(Constants.SharedPreferences.NOTIFICATIONS_SAVED_APID, channelId);
    }

    private String getSavedChannelId() {
        return getPreferences().readString(Constants.SharedPreferences.NOTIFICATIONS_SAVED_APID);
    }

    //endregion


    //region Push Captcha

    @Override
    public String getPushToken() {
        if (BuildConfig.DEBUG)
            return null;
        else
            return UAirship.shared().getPushManager().getChannelId();
    }

    //endregion


    //region Registration

    private boolean isHostSafe(String host) {
        return (!BuildConfig.DEBUG || (!"https://api.livenation.com".equals(host) && !"https://prod-faceoff.herokuapp.com".equals(host)));
    }

    public boolean shouldRegister() {
        String channelId = UAirship.shared().getPushManager().getChannelId();
        String userId = RichPushManager.shared().getRichPushUser().getId();
        return ((channelId != null && !channelId.equals(getSavedChannelId())) && userId != null);
    }

    public void register() {

        if (!isHostSafe(LiveNationLibrary.getHost())) {
            Log.e(getClass().getName(), "Ignoring unsafe host: " + LiveNationLibrary.getHost());
            return;
        }

        final String channelId = UAirship.shared().getPushManager().getChannelId();
        final String userId = RichPushManager.shared().getRichPushUser().getId();

        Log.i(getClass().getName(), "Registering with platform with channelId: " + channelId + ", UA user id: " + userId);

        RegisterForNotificationsParameters params = new RegisterForNotificationsParameters();
        params.setTokens(channelId, userId);
        LiveNationApplication.getLiveNationProxy().registerForNotifications(params, new BasicApiCallback<Void>() {
            @Override
            public void onErrorResponse(LiveNationError error) {
                String errorMessage = (error != null) ? error.getMessage() : "unknown error";
                Log.e(getClass().getName(), "Could not register with platform: " + errorMessage, error);
            }

            @Override
            public void onResponse(Void response) {
                saveChannelId(channelId);
                Log.i(getClass().getName(), "Completed platform registration");
            }
        });
    }

    //endregion
}
