package com.livenation.mobile.android.na.providers;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.livenation.mobile.android.platform.init.provider.DeviceIdProvider;
import com.livenation.mobile.android.platform.init.provider.ProviderCallback;

import java.io.IOException;
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
            } catch (IOException e) {
                callback.onResponse(UUID.randomUUID().toString());
            } catch (GooglePlayServicesNotAvailableException e) {
                callback.onResponse(UUID.randomUUID().toString());
            } catch (GooglePlayServicesRepairableException e) {
                callback.onResponse(UUID.randomUUID().toString());
            }
        }
    }
}
