package com.livenation.mobile.android.na.helpers;

import android.content.Context;

/**
 * Created by cchilton on 3/13/14.
 */
public class SystemLocationProvider implements LocationProvider {
    private final LocationProvider playServices = new PlayServicesLocationProvider();
    private final LocationProvider device = new DeviceLocationProvider();
    private final LocationProvider dummy = new DummyLocationProvider();

    @Override
    public void getLocation(Context context, LocationManager.LocationCallback callback) {
        playServices.getLocation(context, new LocationProxy(callback, playServices, context));
    }

    private void onProviderFailed(LocationProxy locationProxy) {
        LocationProvider provider = locationProxy.getLocationProvider();
        if (provider == playServices) {
            Context context = locationProxy.getContext();
            LocationCallback callback = locationProxy.getCallback();
            device.getLocation(context, new LocationProxy(callback, device, context));
        }

        if (provider == device) {
            Context context = locationProxy.getContext();
            LocationCallback callback = locationProxy.getCallback();
            dummy.getLocation(context, new LocationProxy(callback, dummy, context));
        }
    }

    private class LocationProxy implements LocationCallback {
        private final LocationCallback callback;
        private final LocationProvider locationProvider;
        private final Context context;

        private LocationProxy(LocationCallback callback, LocationProvider locationProvider, Context context) {
            this.callback = callback;
            this.locationProvider = locationProvider;
            this.context = context;
        }

        @Override
        public void onLocation(double lat, double lng) {
            callback.onLocation(lat, lng);
        }

        @Override
        public void onLocationFailure(int failureCode) {
            onProviderFailed(LocationProxy.this);
        }

        public LocationProvider getLocationProvider() {
            return locationProvider;
        }

        public Context getContext() {
            return context;
        }

        public LocationCallback getCallback() {
            return callback;
        }
    }
}
