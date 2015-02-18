package com.livenation.mobile.android.na.providers;

import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by cchilton on 3/13/14.
 */
public class UserLocationAppProvider implements LocationProvider {
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private final Context context;

    public UserLocationAppProvider(Context context) {
        this.context = context;
    }

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        PreferencePersistence prefs = new PreferencePersistence("user_location", context);
        String latValue = prefs.readString(KEY_LAT);
        String lngValue = prefs.readString(KEY_LNG);
        if (TextUtils.isEmpty(latValue) || TextUtils.isEmpty(lngValue)) {
            callback.onErrorResponse();
        } else {
            callback.onResponse(new Double[]{Double.valueOf(latValue), Double.valueOf(lngValue)});
        }
    }

    public void setLocation(double lat, double lng, Context context) {
        PreferencePersistence prefs = new PreferencePersistence("user_location", context);
        prefs.write(KEY_LAT, Double.valueOf(lat).toString());
        prefs.write(KEY_LNG, Double.valueOf(lng).toString());
    }

}
