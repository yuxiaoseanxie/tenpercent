package com.livenation.mobile.android.na.apiconfig;

import android.content.Context;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;

/**
 * Created by cchilton on 5/5/14.
 */
class HostConfig extends ApiBuilderElement<String> {

    private final Context context;

    public HostConfig(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        Constants.Environment environment = ConfigManager.getConfiguredEnvironment(context);
        setResult(environment.getHost());
        notifyReady();
    }
}
