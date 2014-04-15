package com.livenation.mobile.android.na.notifications;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.ApiHelper;
import com.livenation.mobile.android.na.helpers.PersistenceProvider;
import com.livenation.mobile.android.na.helpers.PreferencePersistence;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;

public class NotificationsRegistrationManager implements ApiServiceBinder {
    private Context applicationContext;
    private LiveNationApiService apiService;
    private boolean registerAfterServiceBind;

    //region Lifecycle

    private static final NotificationsRegistrationManager instance = new NotificationsRegistrationManager();

    private NotificationsRegistrationManager() {
        this.applicationContext = LiveNationApplication.get();
        getApiHelper().persistentBindApi(this);
    }

    public static NotificationsRegistrationManager getInstance() {
        return instance;
    }

    private ApiHelper getApiHelper() {
        return LiveNationApplication.get().getApiHelper();
    }

    //endregion


    //region Persistence

    private PersistenceProvider<String> getPreferences() {
        return new PreferencePersistence("notifications");
    }

    private void saveApid(String apid) {
        Log.i(getClass().getName(), "saved apid");
        getPreferences().write(Constants.SharedPreferences.NOTIFICATIONS_SAVED_APID, apid, applicationContext);
    }

    private String getSavedApid() {
        return getPreferences().read(Constants.SharedPreferences.NOTIFICATIONS_SAVED_APID, applicationContext);
    }

    //endregion


    //region ApiServiceBinder

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        this.apiService = apiService;
        if(registerAfterServiceBind)
            register();
    }

    //endregion


    //region Registration

    public boolean shouldRegister() {
        String apid = PushManager.shared().getAPID();
        String userId = RichPushManager.shared().getRichPushUser().getId();
        return ((apid != null && !apid.equals(getSavedApid())) && userId != null);
    }

    public void register() {
        if (apiService == null) {
            registerAfterServiceBind = true;
            return;
        }

        final String apid = PushManager.shared().getAPID();
        final String userId = RichPushManager.shared().getRichPushUser().getId();

        Log.i(getClass().getName(), "Registering with platform with apid: " + apid + ", UA user id: " + userId);

        ApiParameters.RegisterForNotificationsParameters params = new ApiParameters.RegisterForNotificationsParameters();
        params.setDeviceId(apiService.getApiConfig().getDeviceId());
        params.setTokens(apid, userId);
        apiService.registerForNotifications(params, new ApiService.BasicApiCallback<Void>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(getClass().getName(), "Could not register with platform: " + new String(error.networkResponse.data));
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
