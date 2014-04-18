package com.livenation.mobile.android.na.helpers;

import android.content.Context;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;


/**
 * Created by cchilton on 3/13/14.
 */
public class SystemLocationProvider implements LocationProvider {
    private final LocationProvider playServices = new PlayServicesLocationProvider();
    private final LocationProvider device = new DeviceLocationProvider();
    private final LocationProvider dummy = new DummyLocationProvider();

    @Override
    public void getLocation(Context context, ApiService.BasicApiCallback<Double[]> callback) {
        playServices.getLocation(context, new LocationProxy(callback, playServices, context));
    }

    private void onProviderFailed(LocationProxy locationProxy) {
        LocationProvider provider = locationProxy.getLocationProvider();
        if (provider == playServices) {
            Context context = locationProxy.getContext();
            ApiService.BasicApiCallback<Double[]> callback = locationProxy.getCallback();
            device.getLocation(context, new LocationProxy(callback, device, context));
        }

        if (provider == device) {
            Context context = locationProxy.getContext();
            ApiService.BasicApiCallback<Double[]> callback = locationProxy.getCallback();
            dummy.getLocation(context, new LocationProxy(callback, dummy, context));
        }
    }

    private class LocationProxy implements ApiService.BasicApiCallback<Double[]> {
        private final ApiService.BasicApiCallback<Double[]> callback;
        private final LocationProvider locationProvider;
        private final Context context;

        private LocationProxy(ApiService.BasicApiCallback<Double[]> callback, LocationProvider locationProvider, Context context) {
            this.callback = callback;
            this.locationProvider = locationProvider;
            this.context = context;
        }

        @Override
        public void onResponse(Double[] location) {
            callback.onResponse(location);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            onProviderFailed(LocationProxy.this);
        }

        public LocationProvider getLocationProvider() {
            return locationProvider;
        }

        public Context getContext() {
            return context;
        }

        public ApiService.BasicApiCallback<Double[]> getCallback() {
            return callback;
        }
    }
}
