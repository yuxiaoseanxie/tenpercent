package com.mock;

import android.mobile.livenation.com.livenationui.provider.location.LocationManager;
import android.mobile.livenation.com.livenationui.provider.location.ReverseGeocode;

import android.content.Context;

/**
 * Created by elodieferrais on 2/6/15.
 */
public class LocationManagerWithReverseGeocodeMocked extends LocationManager {
    public LocationManagerWithReverseGeocodeMocked(Context context) {
        super(context);
    }

    @Override
    protected void reverseGeocodeCity(double lat, double lng, ReverseGeocode.GetCityCallback callback) {
        ReverseGeocodeMock task = new ReverseGeocodeMock(context, lat, lng, callback);
        task.execute();
    }
}
