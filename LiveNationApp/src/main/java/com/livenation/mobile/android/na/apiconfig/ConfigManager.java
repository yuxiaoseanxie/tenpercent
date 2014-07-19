package com.livenation.mobile.android.na.apiconfig;

import android.content.Context;

import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 3/10/14.
 */
public class ConfigManager {
    private final Context context;
    private final SsoManager ssoManager;

    public ConfigManager(Context context, SsoManager ssoManager) {
        this.context = context.getApplicationContext();
        this.ssoManager = ssoManager;
    }


}
