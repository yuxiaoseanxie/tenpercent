/*
 * 
 * @author Charlie Chilton 2014/01/27
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */
package com.livenation.mobile.android.na.providers.location;

import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

/**
 * Dummy location provider to give a dummy location as a last resort.
 * <p/>
 * For development purposes.
 *
 * @author cchilton
 */
public class DummyLocationProvider implements LocationProvider {
    public static final Double[] LOCATION_SF = new Double[]{37.7833, -122.4167};
    private static final String TAG = "DUMMY_LOCATION_PROVIDER";

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        callback.onResponse(LOCATION_SF);
    }
}
