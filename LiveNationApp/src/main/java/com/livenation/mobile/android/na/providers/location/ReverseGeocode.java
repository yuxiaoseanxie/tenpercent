package com.livenation.mobile.android.na.providers.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by elodieferrais on 2/6/15.
 */
public class ReverseGeocode extends AsyncTask<Void, Void, String> {
    private final Context context;
    protected final GetCityCallback callback;
    protected final Double lat;
    protected final Double lng;

    public ReverseGeocode(Context context, double lat, double lng, GetCityCallback callback) {
        this.context = context;
        this.callback = callback;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    protected String doInBackground(Void... params) {
        if (lat == null || lng == null) {
            return null;
        }
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
                                locality = LocationManager.UNKNOWN_LOCATION + " " + String.valueOf(lat) + "," + String.valueOf(lng);
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
        if (callback == null) {
            return;
        }
        if (null != value) {
            City city = new City(value, lat, lng);
            callback.onGetCity(city);
        } else {
            callback.onGetCityFailure(lat, lng);
        }
    }

    public static interface GetCityCallback {
        void onGetCity(City city);

        void onGetCityFailure(double lat, double lng);
    }
}