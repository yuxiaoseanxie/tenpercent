package com.livenation.mobile.android.na.providers;

import android.content.Context;

import com.livenation.mobile.android.na.helpers.PlayServicesLocationProvider;
import com.livenation.mobile.android.na.providers.location.DeviceLocationProvider;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;


/**
 * Created by cchilton on 3/13/14.
 */
public class SystemLocationAppProvider implements LocationProvider {
    private final LocationProvider playServices = new PlayServicesLocationProvider();
    private final LocationProvider device = new DeviceLocationProvider();
    private Double[] cacheLocation;

    @Override
    public void getLocation(final ProviderCallback<Double[]> callback) {
        if (cacheLocation != null) {
            callback.onResponse(cacheLocation);
        } else {
            getLocationWith(playServices, callback);
        }
    }

    public void clearActualLocationCache() {
        cacheLocation = null;
    }

    private void getLocationWith(final LocationProvider provider, final ProviderCallback<Double[]> callback) {
        provider.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                cacheLocation = response;
                callback.onResponse(response);
            }

            @Override
            public void onErrorResponse() {
                onProviderFailedWith(provider, callback);
            }
        });
    }

    private void onProviderFailedWith(final LocationProvider provider, final ProviderCallback<Double[]> callback) {
        if (provider instanceof SystemLocationAppProvider) {
            getLocationWith(device, callback);

        } else {
            callback.onErrorResponse();
        }
    }
}
