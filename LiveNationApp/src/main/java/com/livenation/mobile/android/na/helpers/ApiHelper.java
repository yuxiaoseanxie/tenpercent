package com.livenation.mobile.android.na.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Debug;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.SsoActivity;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
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
        if (null != apiService && !isBuildingApi()) {
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

        LiveNationApiBuilder apiBuilder = new LiveNationApiBuilder(ssoProvider);
        apiBuilder.getSsoToken().addListener(new SsoTokenListener(apiBuilder));

        Activity activity = ssoManager.getActivity();
        if (null != activity) {
            WeakReference<Activity> weakActivity = new WeakReference<Activity>(activity);
            apiBuilder.getActivity().setResult(weakActivity);
        }

        return apiBuilder;
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
