package com.livenation.mobile.android.na.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.platform.init.provider.DeviceIdProvider;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;

import java.util.UUID;

/**
 * Created by elodieferrais on 4/11/14.
 */
public class DeviceIdProviderImpl implements DeviceIdProvider {
    private final Context appContext;

    public DeviceIdProviderImpl(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public void getDeviceId(ProviderCallback<String> callback) {
        GetAdvertisingId advertisingId = new GetAdvertisingId(callback);
        new Thread(advertisingId).start();
    }

    private class GetAdvertisingId implements Runnable {

        final private ProviderCallback<String> callback;

        private GetAdvertisingId(ProviderCallback<String> callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            AdvertisingIdClient.Info adInfo;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(appContext);
                final String id = adInfo.getId();
                callback.onResponse(id);
            } catch (Exception e) {
                //Getting the Google Play Services Advertising ID Failed.
                //Retrieve a UUID from preferences
                SharedPreferences prefs = appContext.getSharedPreferences(Constants.SharedPreferences.DEVICE_UUID, Context.MODE_PRIVATE);
                String uuid = prefs.getString(Constants.SharedPreferences.DEVICE_UUID, null);
                if (TextUtils.isEmpty(uuid)) {
                    //no existing UUID, generate and save a new one.
                    uuid = UUID.randomUUID().toString();
                    //store new UUID
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Constants.SharedPreferences.DEVICE_UUID, uuid);
                    editor.apply();
                }

                callback.onResponse(uuid);
            }
        }
    }
}
