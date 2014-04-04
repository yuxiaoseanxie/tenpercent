package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by cchilton on 3/13/14.
 */
public class UserLocationProvider implements LocationProvider {
    public static final int FAILURE_NO_USER_LOCATION_SET = 1000;
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";

    @Override
    public void getLocation(Context context, LocationCallback callback) {
        PersistenceProvider<String> prefs = new PreferencePersistence("user_location");
        String latValue = prefs.read(KEY_LAT, context);
        String lngValue = prefs.read(KEY_LNG, context);
        if (TextUtils.isEmpty(latValue) || TextUtils.isEmpty(lngValue)) {
            callback.onLocationFailure(FAILURE_NO_USER_LOCATION_SET);
        } else {
            callback.onLocation(Double.valueOf(latValue), Double.valueOf(lngValue));
        }
    }

    public void setLocation(double lat, double lng, Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence("user_location");
        prefs.write(KEY_LAT, Double.valueOf(lat).toString(), context);
        prefs.write(KEY_LNG, Double.valueOf(lng).toString(), context);
    }

}
