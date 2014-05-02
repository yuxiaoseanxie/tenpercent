package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Pair;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiConfig;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.LiveNationApiServiceImpl;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.ContextConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by cchilton on 3/10/14.
 */
public class ApiHelper implements ApiBuilder.OnBuildListener {
    private final Context context;
    private LiveNationApiBuilder apiBuilder;
    //pending bindings are those objects who tried to bind to the api before it was created
    private List<ApiServiceBinder> pendingBindings = new ArrayList<ApiServiceBinder>();
    //persistent bindings are objects who want to be persistently updated of new API objects,
    //eg favoritesObserverPresenter, who will clear its favorite cache when a new API is created
    private List<ApiServiceBinder> persistentBindings = new ArrayList<ApiServiceBinder>();
    private LiveNationApiService apiService;

    public ApiHelper(Context context) {
        this.context = context.getApplicationContext();
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
        Logger.log("ApiHelper", "Already building");
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
        apiBuilder.build(ApiHelper.this);
    }

    public void clearAccessToken(Context context) {
        AccessTokenPersistenceHelper.clearAccessToken(context);
    }

    private void saveAccessToken(String accessToken, Context context) {
        AccessTokenPersistenceHelper.saveAccessToken(accessToken, context);
    }

    private String readAccessToken(Context context) {
        return AccessTokenPersistenceHelper.readAccessToken(context);
    }

    private LiveNationApiBuilder createApiBuilder() {
        ApiBuilderElement<String> deviceId = new DeviceIdConfig();
        ApiBuilderElement<Context> context = new ContextConfig(this.context);
        ApiBuilderElement<String> host = new HostConfig();
        ApiBuilderElement<String> clientId = new ClientIdConfig();
        ApiBuilderElement<Double[]> location = new LocationConfig();
        ApiBuilderElement<String> accessToken = new AccessTokenConfig();

        LiveNationApiBuilder apiBuilder = new LiveNationApiBuilder(host, clientId, deviceId, accessToken, location, context);

        return apiBuilder;
    }

    private class HostConfig extends ApiBuilderElement<String> {

        @Override
        public void run() {
            super.run();
            Constants.Environment environment = ApiHelper.getConfiguredEnvironment(context);
            setResult(environment.getHost());
            notifyReady();
        }
    }

    private class ClientIdConfig extends ApiBuilderElement<String> {

        @Override
        public void run() {
            super.run();
            Constants.Environment environment = ApiHelper.getConfiguredEnvironment(context);
            setResult(environment.getClientId());
            notifyReady();
        }
    }

    private class DeviceIdConfig extends ApiBuilderElement<String> {
        private final String PREFS_DEVICE_UUID = "device_uuid";

        @Override
        public void run() {
            new Thread(new GetAdvertisingId()).start();
        }

        private class GetAdvertisingId implements Runnable {
            @Override
            public void run() {
                AdvertisingIdClient.Info adInfo = null;
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    final String id = adInfo.getId();
                    setResult(id);

                } catch (Exception e) {
                    //Getting the Google Play Services Advertising ID Failed.
                    //Retrieve a UUID from preferences
                    SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPreferences.DEVICE_UUID, Context.MODE_PRIVATE);
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


    private class LocationConfig extends ApiBuilderElement<Double[]> implements LocationProvider.LocationCallback {

        @Override
        public void run() {
            super.run();
            LiveNationApplication.get().getLocationManager().getLocation(context, this);
        }

        @Override
        public void onLocation(final double lat, final double lng) {
            Double[] locationValue = new Double[2];
            locationValue[0] = lat;
            locationValue[1] = lng;
            setResult(locationValue);
            notifyReady();
            //add the location to our "location history" list
            LiveNationApplication.get().getLocationManager().reverseGeocodeCity(lat, lng, context, new LocationManager.GetCityCallback() {
                @Override
                public void onGetCity(City city) {
                    LiveNationApplication.get().getLocationManager().addLocationHistory(city, context);
                }

                @Override
                public void onGetCityFailure() {
                    String label = context.getString(R.string.location_unknown);
                    City city = new City(label, lat, lng);
                    LiveNationApplication.get().getLocationManager().addLocationHistory(city, context);
                }
            });
        }

        @Override
        public void onLocationFailure(int failureCode) {
            notifyFailed(0, "");
        }
    }

    private class AccessTokenConfig extends ApiBuilderElement<String> implements ApiService.BasicApiCallback<AccessToken> {

        @Override
        public void run() {
            super.run();
            String accessToken = readAccessToken(context);

            if (TextUtils.isEmpty(accessToken)) {
                retrieveToken();
            } else {
                setResult(accessToken);
                notifyReady();
            }
        }

        @Override
        public void onResponse(AccessToken response) {
            String token = response.getToken();
            saveAccessToken(token, context);
            setResult(token);
            notifyReady();
        }

        @Override
        public void onErrorResponse(LiveNationError error) {
            notifyFailed(error.getErrorCode(), error.getMessage());
        }

        private void retrieveToken() {
            LiveNationApiBuilder builder = (LiveNationApiBuilder) getApiBuilder();

            String host = builder.getHost().getResult();
            String clientId = builder.getClientId().getResult();
            String deviceId = builder.getDeviceId().getResult();
            Pair<String, String> ssoParams = getSsoParams();

            LiveNationApiConfig quick = new LiveNationApiConfig(
                    host, clientId, deviceId,
                    null,
                    0, 0,
                    builder.getContext().getResult());

            LiveNationApiService apiService = new LiveNationApiServiceImpl(quick);
            apiService.getToken(clientId, deviceId, ssoParams.first, ssoParams.second, AccessTokenConfig.this);
        }

        private Pair<String, String> getSsoParams() {
            SsoManager.AuthConfiguration ssoConfig = LiveNationApplication.get().getSsoManager().getAuthConfiguration(ApiHelper.this.context);
            if (ssoConfig != null) {
                int ssoProviderId = ssoConfig.getSsoProviderId();
                String key = LiveNationApplication.get().getSsoManager().getSsoProvider(ssoProviderId, ApiHelper.this.context).getTokenKey();
                String value = ssoConfig.getAccessToken();
                return new Pair<String, String>(key, value);
            }
            return new Pair<String, String>(null, null);
        }
    }

    private static class AccessTokenPersistenceHelper {

        public static void saveAccessToken(String accessToken, Context context) {
            PreferencePersistence prefs = new PreferencePersistence(Constants.SharedPreferences.API_NAME);
            prefs.write(Constants.SharedPreferences.API_ACCESS_TOKEN, accessToken, context);
        }

        public static void clearAccessToken(Context context) {
            PreferencePersistence prefs = new PreferencePersistence(Constants.SharedPreferences.API_NAME);
            prefs.remove(Constants.SharedPreferences.API_ACCESS_TOKEN, context);
        }

        public static String readAccessToken(Context context) {
            PreferencePersistence prefs = new PreferencePersistence(Constants.SharedPreferences.API_NAME);
            return prefs.read(Constants.SharedPreferences.API_ACCESS_TOKEN, context);
        }

    }
}
