package com.livenation.mobile.android.na.providers;

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
    private static Double[] cacheGPSLocation;

    @Override
    public void getLocation(final ProviderCallback<Double[]> callback) {
        if (cacheGPSLocation != null) {
            callback.onResponse(cacheGPSLocation);
        } else {
            getLocationWith(playServices, callback);
        }
    }

    private void getLocationWith(final LocationProvider provider, final ProviderCallback<Double[]> callback) {
        provider.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                cacheGPSLocation = response;
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
