package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.ui.SsoActivity;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.ContextConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.SsoProviderConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.StringValueConfig;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;
import com.livenation.mobile.android.platform.util.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by cchilton on 3/10/14.
 */
public class ApiHelper implements ApiBuilder.OnBuildListener {
    private LiveNationApiBuilder apiBuilder;

    //pending bindings are those objects who tried to bind to the api before it was created
    private List<ApiServiceBinder> pendingBindings = new ArrayList<ApiServiceBinder>();
    //persistent bindings are objects who want to be persistently updated of new API objects,
    //eg favoritesObserverPresenter, who will clear its favorite cache when a new API is created
    private List<ApiServiceBinder> persistentBindings = new ArrayList<ApiServiceBinder>();
    private LiveNationApiService apiService;
    private final SsoManager ssoManager;
    private final Context appContext;

    public ApiHelper(SsoManager ssoManager, Context appContext) {
        this.ssoManager = ssoManager;
        this.appContext = appContext;
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

    public void setActivityDependency(Activity activity) {
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


        LiveNationApiBuilder apiBuilder = new LiveNationApiBuilder(host, clientId, deviceId, ssoProvider, context);
        apiBuilder.getSsoToken().addListener(new SsoTokenListener(apiBuilder));

        Activity activity = ssoManager.getActivity();
        if (null != activity) {
            WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
            apiBuilder.getActivity().setResult(weakActivity);
        }

        return apiBuilder;
    }

    public static Constants.Environment getConfiguredEnvironment(Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence("environment");
        String environmentKey = prefs.read("environment", context);

        try {
            return Constants.Environment.valueOf(environmentKey);
        } catch (Exception e) {
            return Constants.Environment.StagingDirect;
        }
    }

    public static void setConfiguredEnvironment(Constants.Environment environment, Context context) {
        PersistenceProvider<String> prefs = new PreferencePersistence("environment");
        prefs.write("environment", environment.toString(), context);
    }

    private class GetDeviceId extends ApiBuilderElement<String> {
        private final Context appContext;

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
                } catch (IOException e) {
                    setResult(UUID.randomUUID().toString());
                } catch (GooglePlayServicesNotAvailableException e) {
                    setResult(UUID.randomUUID().toString());
                } catch (GooglePlayServicesRepairableException e) {
                    setResult(UUID.randomUUID().toString());
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

        @Override
        public void onInvalidated(ApiBuilderElement element) {

        }
    }
}
