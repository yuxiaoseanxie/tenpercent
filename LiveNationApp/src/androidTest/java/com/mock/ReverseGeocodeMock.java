package com.mock;

import android.mobile.livenation.com.livenationui.provider.location.ReverseGeocode;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

import android.content.Context;

/**
 * Created by elodieferrais on 2/6/15.
 */
public class ReverseGeocodeMock extends ReverseGeocode {
    public static final String DEFAULT_NAME = "default name";

    public ReverseGeocodeMock(Context context, double lat, double lng, GetCityCallback callback) {
        super(context, lat, lng, callback);
    }


    @Override
    protected String doInBackground(Void... params) {
        return DEFAULT_NAME;
    }

    @Override
    protected void onPostExecute(String s) {
        callback.onGetCity(new City(DEFAULT_NAME, lat, lng));
    }
}
