package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.text.TextUtils;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

/**
 * Created by cchilton on 3/13/14.
 */
public class UserLocationProvider implements LocationProvider {
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        PreferencePersistence prefs = new PreferencePersistence("user_location");
        String latValue = prefs.readString(KEY_LAT, LiveNationApplication.get().getApplicationContext());
        String lngValue = prefs.readString(KEY_LNG, LiveNationApplication.get().getApplicationContext());
        if (TextUtils.isEmpty(latValue) || TextUtils.isEmpty(lngValue)) {
            callback.onErrorResponse();
        } else {
            callback.onResponse(new Double[]{Double.valueOf(latValue), Double.valueOf(lngValue)});
        }
    }

    public void setLocation(double lat, double lng, Context context) {
        PreferencePersistence prefs = new PreferencePersistence("user_location");
        prefs.write(KEY_LAT, Double.valueOf(lat).toString(), context);
        prefs.write(KEY_LNG, Double.valueOf(lng).toString(), context);
    }

}
