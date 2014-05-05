package com.livenation.mobile.android.na.apiconfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;

import java.util.UUID;

/**
 * Created by cchilton on 5/5/14.
 */
class DeviceIdConfig extends ApiBuilderElement<String> {
    private final String PREFS_DEVICE_UUID = "device_uuid";
    private Context context;

    public DeviceIdConfig(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        new Thread(new GetAdvertisingId()).start();
    }

    private class GetAdvertisingId implements Runnable {
        @Override
        public void run() {
            AdvertisingIdClient.Info adInfo = null;
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                final String id = adInfo.getId();
                setResult(id);

            } catch (Exception e) {
                //Getting the Google Play Services Advertising ID Failed.
                //Retrieve a UUID from preferences
                SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPreferences.DEVICE_UUID, Context.MODE_PRIVATE);
                String uuid = prefs.getString(PREFS_DEVICE_UUID, null);
                if (TextUtils.isEmpty(uuid)) {
                    //no existing UUID, generate and save a new one.
                    uuid = UUID.randomUUID().toString();
                    //store new UUID
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREFS_DEVICE_UUID, uuid);
                    editor.apply();
                }
                setResult(uuid);
            }
            notifyReady();
        }
    }
}
