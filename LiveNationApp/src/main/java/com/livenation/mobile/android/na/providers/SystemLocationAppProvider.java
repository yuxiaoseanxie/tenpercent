package com.livenation.mobile.android.na.providers;

import android.content.Context;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.DeviceLocationProvider;
import com.livenation.mobile.android.na.helpers.DummyLocationProvider;
import com.livenation.mobile.android.na.helpers.PlayServicesLocationProvider;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;


/**
 * Created by cchilton on 3/13/14.
 */
public class SystemLocationAppProvider implements LocationProvider {
    private final LocationProvider playServices = new PlayServicesLocationProvider();
    private final LocationProvider device = new DeviceLocationProvider();
    private final LocationProvider dummy = new DummyLocationProvider();

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        playServices.getLocation(new LocationProxy(callback, playServices, LiveNationApplication.get().getApplicationContext()));
    }

    private void onProviderFailed(LocationProxy locationProxy) {
        LocationProvider provider = locationProxy.getLocationProvider();
        if (provider == playServices) {
            Context context = locationProxy.getContext();
            ProviderCallback<Double[]> callback = locationProxy.getCallback();
            device.getLocation(new LocationProxy(callback, device, context));
        }

        if (provider == device) {
            Context context = locationProxy.getContext();
            ProviderCallback<Double[]> callback = locationProxy.getCallback();
            dummy.getLocation(new LocationProxy(callback, dummy, context));
        }

        if (provider == dummy) {
            locationProxy.getCallback().onErrorResponse();
        }
    }

    private class LocationProxy implements ProviderCallback<Double[]> {
        private final ProviderCallback<Double[]> callback;
        private final LocationProvider locationProvider;
        private final Context context;

        private LocationProxy(ProviderCallback<Double[]> callback, LocationProvider locationProvider, Context context) {
            this.callback = callback;
            this.locationProvider = locationProvider;
            this.context = context;
        }

        @Override
        public void onResponse(Double[] location) {
            callback.onResponse(location);
        }

        @Override
        public void onErrorResponse() {
            onProviderFailed(LocationProxy.this);
        }

        public LocationProvider getLocationProvider() {
            return locationProvider;
        }

        public Context getContext() {
            return context;
        }

        public ProviderCallback<Double[]> getCallback() {
            return callback;
        }
    }
}
