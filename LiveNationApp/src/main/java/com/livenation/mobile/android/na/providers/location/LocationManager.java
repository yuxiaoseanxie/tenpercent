package com.livenation.mobile.android.na.providers.location;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationUpdateReceiver;
import com.livenation.mobile.android.na.helpers.VisibleForTesting;
import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.na.providers.SystemLocationAppProvider;
import com.livenation.mobile.android.na.providers.UserLocationAppProvider;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
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
    public static final int MODE_UNKNOWN_BECAUSE_ERROR = 2;
    public static final String LOCATION_MODE = "location_mode";
    public static final Double[] DEFAULT_LOCATION = new Double[]{37.7833, -122.4167};
    public static final String DEFAULT_LOCATION_NAME = "San Francisco";
    public static final String UNKNOWN_LOCATION = "Unknown";

    private LocationProvider userLocationProvider;
    private LocationProvider systemLocationProvider;
    private final LocationHistoryManager locationHistory;
    private final Context context;
    private Integer mode = null;

    public LocationManager(Context context) {
        this.context = context.getApplicationContext();
        userLocationProvider = new UserLocationAppProvider(context);
        systemLocationProvider = new SystemLocationAppProvider();
        locationHistory = new LocationHistoryManager(context);
    }

    @VisibleForTesting
    public void setSystemLocationProvider(LocationProvider systemLocationProvider) {
        this.systemLocationProvider = systemLocationProvider;
    }

    @VisibleForTesting
    public void setUserLocationProvider(LocationProvider userLocationProvider) {
        this.userLocationProvider = userLocationProvider;
    }

    public void getSystemLocation(final BasicApiCallback<City> callback) {
        getSystemLocationProvider().getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                getCityWithGPSCoordinates(response, callback);
            }

            @Override
            public void onErrorResponse() {
                callback.onResponse(new City(DEFAULT_LOCATION_NAME, DEFAULT_LOCATION[0], DEFAULT_LOCATION[1]));
            }
        });
    }

    public void getLocation(final BasicApiCallback<City> callback) {
        getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                getCityWithGPSCoordinates(response, callback);
            }

            @Override
            public void onErrorResponse() {
                //Should never be called
                throw new IllegalStateException("The LocationManager did not return any location (not even the default one)");
            }
        });
    }

    private void getCityWithGPSCoordinates(Double[] response, final BasicApiCallback<City> callback) {
        final double lat = response[0];
        final double lng = response[1];
        reverseGeocodeCity(lat, lng, new LocationManager.GetCityCallback() {
            @Override
            public void onGetCity(City city) {
                callback.onResponse(city);
            }

            @Override
            public void onGetCityFailure(double lat, double lng) {
                //reverse geocode failed, make up an "unknown" label name
                String cityName = UNKNOWN_LOCATION;
                if (lat == LocationManager.DEFAULT_LOCATION[0] && lng == LocationManager.DEFAULT_LOCATION[1]) {
                    cityName = LocationManager.DEFAULT_LOCATION_NAME;
                }
                callback.onResponse(new City(cityName, lat, lng));
            }
        });
    }


    @Override
    public void getLocation(final ProviderCallback<Double[]> callback) {
        LocationProvider provider = getLocationProvider();
        if (null == getLocationProvider()) throw new IllegalStateException();
        provider.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                callback.onResponse(response);
            }

            @Override
            public void onErrorResponse() {
                saveLocationMode(MODE_UNKNOWN_BECAUSE_ERROR);
                callback.onResponse(DEFAULT_LOCATION);
            }
        });
    }

    public LocationProvider getLocationProvider() {
        switch (getMode()) {
            case MODE_SYSTEM:
            case MODE_UNKNOWN_BECAUSE_ERROR:
                return systemLocationProvider;
            case MODE_USER:
                return userLocationProvider;
            default:
                clearLocationMode(context);
                return systemLocationProvider;
        }
    }

    public void setLocationMode(int mode) {
        saveLocationMode(mode);
        sendBroadcastForLocation();
    }

    public void setUserLocation(int mode, double lat, double lng) {
        saveLocationMode(mode);
        ((UserLocationAppProvider) userLocationProvider).setLocation(lat, lng, context);
        sendBroadcastForLocation();
    }

    public LocationProvider getSystemLocationProvider() {
        return systemLocationProvider;
    }

    public LocationProvider getUserLocationProvider() {
        return userLocationProvider;
    }

    public void reverseGeocodeCity(double lat, double lng, GetCityCallback callback) {
        Context context = LiveNationApplication.get().getApplicationContext();

        ReverseGeocode task = new ReverseGeocode(context, lat, lng, callback);
        task.execute();
    }

    public void addLocationHistory(City city) {
        locationHistory.addLocationHistory(city);
    }

    public List<City> getLocationHistory() {
        return locationHistory.getLocationHistory();
    }

    public int getMode() {
        if (mode == null) {
            mode = readLocationMode();
        }
        return mode;
    }

    private int readLocationMode() {
        PreferencePersistence prefs = new PreferencePersistence("location", context);
        String value = prefs.readString(LOCATION_MODE);
        if (TextUtils.isEmpty(value)) {
            return MODE_SYSTEM;
        }

        return Integer.valueOf(value);
    }

    private void saveLocationMode(int mode) {
        this.mode = mode;
        PreferencePersistence prefs = new PreferencePersistence("location", context);
        prefs.write(LOCATION_MODE, Integer.valueOf(mode).toString());
    }

    public void clearLocationMode(Context context) {
        PreferencePersistence prefs = new PreferencePersistence("location", context);
        prefs.remove(LOCATION_MODE);
    }

    private synchronized void sendBroadcastForLocation() {
        final Intent intent = new Intent(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER);

        getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                intent.putExtra(LocationUpdateReceiver.EXTRA_MODE_KEY, getMode());
                getCityWithGPSCoordinates(response, new BasicApiCallback<City>() {
                    @Override
                    public void onResponse(City response) {
                        intent.putExtra(LocationUpdateReceiver.EXTRA_CITY, response);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }

                    @Override
                    public void onErrorResponse(LiveNationError error) {
                        //Never called
                    }
                });
            }

            @Override
            public void onErrorResponse() {
            }
        });

    }

    public static interface GetCityCallback {
        void onGetCity(City city);

        void onGetCityFailure(double lat, double lng);
    }

    private class ReverseGeocode extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final GetCityCallback callback;
        private final double lat;
        private final double lng;

        private ReverseGeocode(Context context, double lat, double lng, GetCityCallback callback) {
            this.context = context;
            this.callback = callback;
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        protected String doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            try {
                List<Address> matches = geocoder.getFromLocation(lat, lng, 1);
                if (!matches.isEmpty()) {
                    String locality = matches.get(0).getLocality();
                    if (locality == null) {
                        locality = matches.get(0).getSubLocality();
                        if (locality == null) {
                            locality = matches.get(0).getAdminArea();
                            if (locality == null) {
                                locality = matches.get(0).getCountryName();
                                if (locality == null) {
                                    locality = UNKNOWN_LOCATION + " " + String.valueOf(lat) + "," + String.valueOf(lng);
                                }
                            }
                        }
                    }
                    return locality;
                }
            } catch (IOException ignored) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(String value) {
            if (null != value) {
                City city = new City(value, lat, lng);
                callback.onGetCity(city);
            } else {
                callback.onGetCityFailure(lat, lng);
            }
        }
    }
}
