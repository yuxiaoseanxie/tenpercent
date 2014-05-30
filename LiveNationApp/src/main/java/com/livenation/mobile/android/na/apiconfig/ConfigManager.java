package com.livenation.mobile.android.na.apiconfig;

import android.content.Context;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.PersistenceProvider;
import com.livenation.mobile.android.na.helpers.PreferencePersistence;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.ContextConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;
import com.livenation.mobile.android.platform.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 3/10/14.
 */
public class ConfigManager implements ApiBuilder.OnBuildListener {
    private final Context context;
    private final SsoManager ssoManager;

    private LiveNationApiBuilder apiBuilder;
    //pending bindings are those objects who tried to bind to the api before it was created
    private final List<ApiServiceBinder> pendingBindings = new ArrayList<ApiServiceBinder>();
    //persistent bindings are objects who want to be persistently updated of new API objects,
    //eg favoritesObserverPresenter, who will clear its favorite cache when a new API is created
    private final List<ApiServiceBinder> persistentBindings = new ArrayList<ApiServiceBinder>();
    private LiveNationApiService apiService;

    public ConfigManager(Context context, SsoManager ssoManager) {
        this.context = context.getApplicationContext();
        this.ssoManager = ssoManager;
    }

    public static Constants.Environment getConfiguredEnvironment(Context context) {
        if (!BuildConfig.DEBUG) {
            return Constants.Environment.Production;
        }
        PersistenceProvider<String> prefs = new PreferencePersistence("environment");
        String environmentKey = prefs.read("environment", context);

        try {
            return Constants.Environment.valueOf(environmentKey);
        } catch (Exception e) {
            return Constants.Environment.Staging;
        }
    }

    public static void setConfiguredEnvironment(Constants.Environment environment, Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence("environment");
        prefs.write("environment", environment.toString(), context);
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

    public boolean hasApi() {
        return (null != apiService);
    }

    public boolean isBuildingApi() {
        return (null != apiBuilder);
    }

    public void bindApi(ApiServiceBinder binder) {
        if (null != apiService && !isBuildingApi()) {
            binder.onApiServiceAttached(apiService);
        } else {
            pendingBindings.add(binder);
            if (!isBuildingApi()) {
                buildApi();
            }
        }
    }

    public void persistentBindApi(ApiServiceBinder binder) {
        persistentBindings.add(binder);
        if (null != apiService) {
            binder.onApiServiceAttached(apiService);
        }
    }

    public void persistentUnbindApi(ApiServiceBinder binder) {
        if (persistentBindings.contains(binder)) {
            persistentBindings.remove(binder);
        }
        if (pendingBindings.contains(binder)) {
            pendingBindings.remove(binder);
        }
    }

    public void buildApi() {
        apiBuilder = createApiBuilder();
        apiBuilder.build(ConfigManager.this);
    }

    public void clearAccessToken() {
        AccessTokenConfig.clearAccessToken(context);
    }

    private LiveNationApiBuilder createApiBuilder() {
        ApiBuilderElement<String> deviceId = new DeviceIdConfig(this.context);
        ApiBuilderElement<Context> context = new ContextConfig(this.context);
        ApiBuilderElement<String> host = new HostConfig(this.context);
        ApiBuilderElement<String> clientId = new ClientIdConfig(this.context);
        ApiBuilderElement<Double[]> location = new LocationConfig(this.context);
        ApiBuilderElement<String> accessToken = new AccessTokenConfig(this.context, ssoManager);

        LiveNationApiBuilder apiBuilder = new LiveNationApiBuilder(host, clientId, deviceId, accessToken, location, context);

        return apiBuilder;
    }
}
