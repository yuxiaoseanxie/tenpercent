package com.livenation.mobile.android.na.apiconfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Pair;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiConfig;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.LiveNationApiServiceImpl;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

/**
 * Created by cchilton on 5/5/14.
 */
class AccessTokenConfig extends ApiBuilderElement<String> implements ApiService.BasicApiCallback<AccessToken> {

    private final Context context;
    private final SsoManager ssoManager;

    public AccessTokenConfig(Context context, SsoManager ssoManager) {
        this.context = context;
        this.ssoManager = ssoManager;
    }

    static void clearAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPreferences.API_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

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
        clearIasId();
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
        apiService.getToken(clientId, deviceId, getIasId(), ssoParams.first, ssoParams.second, AccessTokenConfig.this);
    }

    private Pair<String, String> getSsoParams() {
        SsoManager.AuthConfiguration ssoConfig = ssoManager.getAuthConfiguration(context);
        if (ssoConfig != null) {
            SsoManager.SSO_TYPE ssoProviderId = ssoConfig.getSsoProviderId();
            String key = ssoManager.getSsoProvider(ssoProviderId, context).getTokenKey();
            String value = ssoConfig.getAccessToken();
            return new Pair<String, String>(key, value);
        }
        return new Pair<String, String>(null, null);
    }

    private void saveAccessToken(String accessToken, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPreferences.API_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.SharedPreferences.API_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    private String readAccessToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPreferences.API_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.SharedPreferences.API_ACCESS_TOKEN, null);
    }

    private String getIasId() {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.SharedPreferences.INSTALLATION_ID, null);
    }

    private void clearIasId() {
        SharedPreferences oldPrefs = context.getSharedPreferences(Constants.SharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences newPrefs = context.getSharedPreferences(Constants.SharedPreferences.IAS_NAME, Context.MODE_PRIVATE);
        newPrefs.edit().putString(Constants.SharedPreferences.IAS_USER_ID, getIasId()).commit();
        oldPrefs.edit().clear().commit();
    }
}
