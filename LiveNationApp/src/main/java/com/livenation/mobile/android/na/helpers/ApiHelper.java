package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.SsoActivity;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.ContextConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.SsoProviderConfig;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.util.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by cchilton on 3/10/14.
 */
public class ApiHelper implements ApiBuilder.OnBuildListener {
    private final SsoManager ssoManager;
    private final Context appContext;
    private LiveNationApiBuilder apiBuilder;
    //pending bindings are those objects who tried to bind to the api before it was created
    private List<ApiServiceBinder> pendingBindings = new ArrayList<ApiServiceBinder>();
    //persistent bindings are objects who want to be persistently updated of new API objects,
    //eg favoritesObserverPresenter, who will clear its favorite cache when a new API is created
    private List<ApiServiceBinder> persistentBindings = new ArrayList<ApiServiceBinder>();
    private LiveNationApiService apiService;

    public ApiHelper(SsoManager ssoManager, Context appContext) {
        this.ssoManager = ssoManager;
        this.appContext = appContext;
    }

    public static Constants.Environment getConfiguredEnvironment(Context context) {
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
        Logger.log("ApiHelper", "Already building");
    }

    public boolean hasApi() {
        return (null != apiService);
    }

    public boolean isBuildingApi() {
        return (null != apiBuilder);
    }

    public void bindApi(ApiServiceBinder binder) {
        if (null != apiService) {
            binder.onApiServiceAttached(apiService);
        } else {
            pendingBindings.add(binder);
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

    public void buildDefaultApi() {
        apiBuilder = createApiBuilder(ssoManager, appContext);
        build();
    }

    public void buildWithSsoProvider(ApiSsoProvider ssoProvider) {
        createApiBuilder(ssoManager, appContext);

        apiBuilder = createApiBuilder(ssoManager, appContext);
        apiBuilder.getSsoProvider().setResult(ssoProvider);

        build();
    }

    public void setDependencyActivity(Activity activity) {
        ssoManager.setActivity(activity);
        if (null != apiBuilder) {
            WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
            apiBuilder.getActivity().setResult(weakActivity);
            build();
        }
    }

    private void build() {
        if (null == apiBuilder) return;
        apiBuilder.build(ApiHelper.this);
    }

    private LiveNationApiBuilder createApiBuilder(SsoManager ssoManager, Context appContext) {

        ApiSsoProvider ssoProviderObject = ssoManager.getConfiguredSsoProvider(appContext);

        ApiBuilderElement<ApiSsoProvider> ssoProvider = new SsoProviderConfig();
        ssoProvider.setResult(ssoProviderObject);

        ApiBuilderElement<String> deviceId = new GetDeviceId(appContext);
        ApiBuilderElement<Context> context = new ContextConfig(appContext);
        ApiBuilderElement<String> host = new GetHostConfig(appContext);
        ApiBuilderElement<String> clientId = new GetClientIdConfig(appContext);
        ApiBuilderElement<Double[]> location = new LocationConfig(appContext);

        LiveNationApiBuilder apiBuilder = new LiveNationApiBuilder(host, clientId, deviceId, ssoProvider, location, context);
        apiBuilder.getSsoToken().addListener(new SsoTokenListener(apiBuilder));

        Activity activity = ssoManager.getActivity();
        if (null != activity) {
            WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
            apiBuilder.getActivity().setResult(weakActivity);
        }

        return apiBuilder;
    }

    private class GetDeviceId extends ApiBuilderElement<String> {
        private final Context appContext;
        private final String PREFS_DEVICE_UUID = "device_uuid";

        private GetDeviceId(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public void run() {
            new Thread(new GetAdvertisingId()).start();
        }

        private class GetAdvertisingId implements Runnable {
            @Override
            public void run() {
                AdvertisingIdClient.Info adInfo = null;
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(appContext);
                    final String id = adInfo.getId();
                    setResult(id);

                } catch (Exception e) {
                    //Getting the Google Play Services Advertising ID Failed.
                    //Retrieve a UUID from preferences
                    SharedPreferences prefs = appContext.getSharedPreferences(Constants.SharedPreferences.DEVICE_UUID, Context.MODE_PRIVATE);
                    String uuid = prefs.getString(PREFS_DEVICE_UUID, null);
                    if (TextUtils.isEmpty(uuid)) {
                        //no existing UUID, generate and save a new one.
                        uuid = UUID.randomUUID().toString();
                        //store new UUID
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(PREFS_DEVICE_UUID, uuid);
                        editor.apply();
                    }
                    setResult(uuid);
                }
                notifyReady();
            }
        }
    }

    private class GetHostConfig extends ApiBuilderElement<String> {
        private final Context appContext;

        private GetHostConfig(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public void run() {
            super.run();
            Constants.Environment environment = getConfiguredEnvironment(appContext);
            setResult(environment.getHost());
            notifyReady();
        }
    }

    private class GetClientIdConfig extends ApiBuilderElement<String> {
        private final Context appContext;

        private GetClientIdConfig(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public void run() {
            super.run();
            Constants.Environment environment = getConfiguredEnvironment(appContext);
            setResult(environment.getClientId());
            notifyReady();
        }
    }


    private class LocationConfig extends ApiBuilderElement<Double[]> implements LocationProvider.LocationCallback {
        private final Context appContext;

        private LocationConfig(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public void run() {
            super.run();
            LiveNationApplication.get().getLocationManager().getLocation(appContext, this);
        }

        @Override
        public void onLocation(double lat, double lng) {
            Double[] locationValue = new Double[2];
            locationValue[0] = lat;
            locationValue[1] = lng;
            setResult(locationValue);
            notifyReady();
        }

        @Override
        public void onLocationFailure(int failureCode) {
            notifyFailed(0, "");
        }
    }

    private class SsoTokenListener implements ApiBuilderElement.ConfigListener {
        private final LiveNationApiBuilder apiBuilder;

        private SsoTokenListener(LiveNationApiBuilder apiBuilder) {
            this.apiBuilder = apiBuilder;
        }

        @Override
        public void onStart(ApiBuilderElement element) {

        }

        @Override
        public void onReady(ApiBuilderElement element) {

        }

        @Override
        public void onFailed(ApiBuilderElement element, int errorCode, String message) {
            Activity activity = apiBuilder.getActivity().getResult().get();
            Intent ssoRepair = new Intent(activity, SsoActivity.class);
            ssoRepair.putExtra(SsoActivity.ARG_PROVIDER_ID, apiBuilder.getSsoProvider().getResult().getId());
            activity.startActivity(ssoRepair);
        }
    }

}
