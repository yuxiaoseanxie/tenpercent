package com.mock;

import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.DeviceIdProvider;

/**
 * Created by elodieferrais on 7/17/14.
 */
public class DeviceIdProviderMock implements DeviceIdProvider {
    @Override
    public void getDeviceId(ProviderCallback<String> callback) {
        callback.onResponse("test");
    }
}
