package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

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
    private final LocationProvider systemLocationProvider = new SystemLocationProvider();

    private final LocationHistoryManager locationHistory;

    private LocationProvider locationProvider;

    public LocationManager(Context context) {
        int locationMode = readLocationMode(context);
        applyLocationMode(locationMode);
        locationHistory = new LocationHistoryManager(context);
    }

    @Override
    public void getLocation(Context context, LocationCallback callback) {
        if (null == locationProvider) throw new IllegalStateException();
        locationProvider.getLocation(context, callback);
    }

    public void setUserLocation(double lat, double lng, Context context) {
        userLocationProvider.setLocation(lat, lng, context);
    }

    public void setLocationMode(int mode, Context context) {
        saveLocationMode(mode, context);
        applyLocationMode(mode);
    }

    public LocationProvider getSystemLocationProvider() {
        return systemLocationProvider;
    }

    public UserLocationProvider getUserLocationProvider() {
        return userLocationProvider;
    }

    public void reverseGeocodeCity(double lat, double lng, Context context, GetCityCallback callback) {
        ReverseGeocode task = new ReverseGeocode(context, lat, lng, callback);
        task.execute();
    }

    public void addLocationHistory(City city, Context context) {
        locationHistory.addLocationHistory(city, context);
    }

    public List<City> getLocationHistory() {
        return locationHistory.getLocationHistory();
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
                                    locality = context.getString(R.string.location_unknown) + " " + String.valueOf(lat) + "," + String.valueOf(lng);
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
