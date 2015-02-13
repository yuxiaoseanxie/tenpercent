package com.mock;

import com.livenation.mobile.android.platform.init.Environment;
import com.livenation.mobile.android.platform.init.provider.EnvironmentProvider;

/**
 * Created by elodieferrais on 7/22/14.
 */
public class EnvironmentProviderMock implements EnvironmentProvider {
    @Override
    public Environment getEnvironment() {
        return Environment.Staging;
    }
}
