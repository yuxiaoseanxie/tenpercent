package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.receiver.LocationUpdateReceiver;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by cchilton on 3/13/14.
 */
public class LocationManager implements LocationProvider {
    public static final int MODE_SYSTEM = 0;
    public static final int MODE_USER = 1;
    public static final String LOCATION_MODE = "location_mode";

    private final UserLocationProvider userLocationProvider = new UserLocationProvider();
    private final SystemLocationProvider systemLocationProvider = new SystemLocationProvider();
    LocationProvider locationProvider;

    public LocationManager(Context context) {
        int locationMode = readLocationMode(context);
        applyLocationMode(locationMode);
    }

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        if (null == locationProvider) throw new IllegalStateException();
        locationProvider.getLocation(callback);
    }

    public void setLocationMode(int mode, Context context) {
        saveLocationMode(mode, context);
        applyLocationMode(mode);
        sendBroadcastForLocation();
    }

    public void setUserLocation(double lat, double lng, Context context) {
        userLocationProvider.setLocation(lat, lng, context);
        sendBroadcastForLocation();
    }

    public LocationProvider getSystemLocationProvider() {
        return systemLocationProvider;
    }

    public UserLocationProvider getUserLocationProvider() {
        return userLocationProvider;
    }

    public void reverseGeocodeCity(final double lat, final double lng, final Context context, final GetCityCallback callback) {
        ReverseGeocode task = new ReverseGeocode(context, callback);
        task.execute(lat, lng);
    }

    private void applyLocationMode(int mode) {
        switch (mode) {
            case MODE_SYSTEM:
                locationProvider = systemLocationProvider;
                break;
            case MODE_USER:
                locationProvider = userLocationProvider;
                break;
            default:
                throw new IllegalArgumentException("" + mode);
        }
    }

    public int getLocationMode(Context context) {
        int locationMode = readLocationMode(context);
        return locationMode;
    }

    private int readLocationMode(Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence("location");
        String value = prefs.read(LOCATION_MODE, context);
        if (TextUtils.isEmpty(value)) {
            return MODE_SYSTEM;
        }
        return Integer.valueOf(value);
    }

    private void saveLocationMode(int mode, Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence("location");
        prefs.write(LOCATION_MODE, Integer.valueOf(mode).toString(), context);
    }

    public static interface GetCityCallback {
        void onGetCity(String city);

        void onGetCityFailure();
    }

    private class ReverseGeocode extends AsyncTask<Double, Void, String> {
        private final Context context;
        private final GetCityCallback callback;

        private ReverseGeocode(Context context, GetCityCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Double... params) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            double lat = params[0];
            double lng = params[1];

            try {
                List<Address> matches = geocoder.getFromLocation(lat, lng, 1);
                if (!matches.isEmpty()) {
                    return matches.get(0).getLocality();
                }
            } catch (IOException ignored) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String value) {
            if (null != value) {
                callback.onGetCity(value);
            } else {
                callback.onGetCityFailure();
            }
        }
    }

    private void sendBroadcastForLocation() {
        final Intent intent = new Intent(Constants.Receiver.LOCATION_UPDATE_INTENT_FILTER);
        locationProvider.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                int mode = MODE_USER;;
                if (locationProvider instanceof SystemLocationProvider) {
                    mode = MODE_SYSTEM;
                }
                intent.putExtra(LocationUpdateReceiver.EXTRA_MODE_KEY, mode);
                intent.putExtra(LocationUpdateReceiver.EXTRA_LAT_KEY, response[0]);
                intent.putExtra(LocationUpdateReceiver.EXTRA_LNG_KEY, response[1]);
                LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onErrorResponse() {}
        });

    }
}
