package com.livenation.mobile.android.na.apiconfig;

import android.content.Context;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 3/10/14.
 */
public class ConfigManager implements ApiBuilder.OnBuildListener {
    private final Context context;
    private final SsoManager ssoManager;
    //pending bindings are those objects who tried to bind to the api before it was created
    private final List<ApiServiceBinder> pendingBindings = new ArrayList<ApiServiceBinder>();
    //persistent bindings are objects who want to be persistently updated of new API objects,
    //eg favoritesObserverPresenter, who will clear its favorite cache when a new API is created
    private final List<ApiServiceBinder> persistentBindings = new ArrayList<ApiServiceBinder>();
    private LiveNationApiBuilder apiBuilder;
    private LiveNationApiService apiService;

    public ConfigManager(Context context, SsoManager ssoManager) {
        this.context = context.getApplicationContext();
        this.ssoManager = ssoManager;
    }

    @Override
    public void onApiBuilt(LiveNationApiService apiService) {
        this.apiBuilder = null;
        this.apiService = apiService;

        for (ApiServiceBinder binder : pendingBindings) {
            binder.onApiServiceAttached(apiService);
        }
        pendingBindings.clear();

        for (ApiServiceBinder binder : persistentBindings) {
            binder.onApiServiceAttached(apiService);
        }
    }

    @Override
    public void onApiAlreadyBuilding() {
        //do nothing
    }

    @Override
    public void onBuildFailed() {
        this.apiBuilder = null;
        this.apiService = null;

        for (ApiServiceBinder binder : pendingBindings) {
            binder.onApiServiceNotAvailable();
        }
        pendingBindings.clear();

        for (ApiServiceBinder binder : persistentBindings) {
            binder.onApiServiceNotAvailable();
        }
    }
}
