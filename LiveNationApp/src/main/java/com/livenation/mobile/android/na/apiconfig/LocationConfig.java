package com.livenation.mobile.android.na.apiconfig;

import android.content.Context;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.LocationProvider;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;

/**
 * Created by cchilton on 5/5/14.
 */
class LocationConfig extends ApiBuilderElement<Double[]> implements LocationProvider.LocationCallback {

    private final Context context;

    public LocationConfig(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        LiveNationApplication.get().getLocationManager().getLocation(context, this);
    }

    @Override
    public void onLocation(final double lat, final double lng) {
        Double[] locationValue = new Double[2];
        locationValue[0] = lat;
        locationValue[1] = lng;
        setResult(locationValue);
        notifyReady();
        //add the location to our "location history" list
        LiveNationApplication.get().getLocationManager().reverseGeocodeCity(lat, lng, context, new LocationManager.GetCityCallback() {
            @Override
            public void onGetCity(City city) {
                LiveNationApplication.get().getLocationManager().addLocationHistory(city, context);
            }

            @Override
            public void onGetCityFailure() {
                String label = context.getString(R.string.location_unknown);
                City city = new City(label, lat, lng);
                LiveNationApplication.get().getLocationManager().addLocationHistory(city, context);
            }
        });
    }

    @Override
    public void onLocationFailure(int failureCode) {
        notifyFailed(0, "");
    }
}
