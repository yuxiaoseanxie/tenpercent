package com.livenation.mobile.android.na.providers.location;

import com.livenation.mobile.android.na.helpers.LocationUpdateReceiver;
import com.livenation.mobile.android.na.helpers.VisibleForTesting;
import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.na.providers.SystemLocationAppProvider;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

public class LocationManager implements LocationProvider {
    public static final int MODE_SYSTEM = 0;
    public static final int MODE_USER = 1;
    public static final int MODE_UNKNOWN_BECAUSE_ERROR = 2;
    public static final String LOCATION_MODE = "location_mode";
    public static final Double[] DEFAULT_LOCATION = new Double[]{37.7833, -122.4167};
    public static final String DEFAULT_LOCATION_NAME = "San Francisco";
    public static final String UNKNOWN_LOCATION = "Unknown";

    private LocationProvider systemLocationProvider;
    private final LocationHistoryManager locationHistory;
    protected final Context context;
    private Double[] cacheSystemLocation;
    private Integer mode = null;

    public LocationManager(Context context) {
        this.context = context;
        systemLocationProvider = new SystemLocationAppProvider();
        locationHistory = new LocationHistoryManager(context);
    }

    @VisibleForTesting
    public void setSystemLocationProvider(LocationProvider systemLocationProvider) {
        this.systemLocationProvider = systemLocationProvider;
    }

    private void getSystemLocation(final BasicApiCallback<Double[]> callback) {
        if (cacheSystemLocation != null) {
            callback.onResponse(cacheSystemLocation);
            return;
        }
        systemLocationProvider.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                cacheSystemLocation = response;
                callback.onResponse(response);
            }

            @Override
            public void onErrorResponse() {
                callback.onResponse(DEFAULT_LOCATION);
            }
        });
    }

    private void getSystemCityLocation(final BasicApiCallback<City> callback) {
        getSystemLocation(new BasicApiCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                getCityWithGPSCoordinates(response, new BasicApiCallback<City>() {
                    @Override
                    public void onResponse(City response) {
                        callback.onResponse(response);
                    }

                    @Override
                    public void onErrorResponse(LiveNationError error) {
                        callback.onErrorResponse(error);
                    }
                });
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        });
    }

    public void getLocation(final BasicApiCallback<City> callback) {
        if (getMode() == LocationManager.MODE_USER) {
            if (getLocationHistory().size() > 0) {
                City city = getLocationHistory().get(0);
                callback.onResponse(city);
                return;
            }
        } else {
            saveLocationMode(LocationManager.MODE_SYSTEM);
        }

        systemLocationProvider.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                getCityWithGPSCoordinates(response, new BasicApiCallback<City>() {
                    @Override
                    public void onResponse(City response) {
                        addLocationHistory(response);
                        saveLocationMode(LocationManager.MODE_SYSTEM);
                        callback.onResponse(response);
                    }

                    @Override
                    public void onErrorResponse(LiveNationError error) {
                        callback.onErrorResponse(error);
                    }
                });
            }

            @Override
            public void onErrorResponse() {
                City city = new City(DEFAULT_LOCATION_NAME, DEFAULT_LOCATION[0], DEFAULT_LOCATION[1]);
                addLocationHistory(city);
                saveLocationMode(MODE_UNKNOWN_BECAUSE_ERROR);
                callback.onResponse(city);
            }
        });
    }

    private void getCityWithGPSCoordinates(Double[] response, final BasicApiCallback<City> callback) {
        final double lat = response[0];
        final double lng = response[1];
        reverseGeocodeCity(lat, lng, new ReverseGeocode.GetCityCallback() {
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
        getLocation(new BasicApiCallback<City>() {
            @Override
            public void onResponse(City response) {
                Double[] result = {response.getLat(), response.getLng()};
                callback.onResponse(result);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse();
            }
        });
    }

    public void setLocationMode(int mode) {
        saveLocationMode(mode);
        sendBroadcastForLocation();
    }

    protected void reverseGeocodeCity(double lat, double lng, ReverseGeocode.GetCityCallback callback) {

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
        mode = null;
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
                //Never called
            }
        });

    }
}
