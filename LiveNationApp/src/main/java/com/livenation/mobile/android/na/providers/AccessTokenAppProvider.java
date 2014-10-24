package com.livenation.mobile.android.na.providers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.apsalar.sdk.Apsalar;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.provider.AccessTokenProvider;
import com.segment.android.models.Props;

/**
 * Created by elodieferrais on 9/29/14.
 */
public class AccessTokenAppProvider extends AccessTokenProvider {

    @Override
    public void getAccessToken(final BasicApiCallback<AccessToken> callback) {
        super.getAccessToken(new BasicApiCallback<AccessToken>() {
            @Override
            public void onResponse(AccessToken response) {
                SharedPreferences oldPrefs = LiveNationApplication.get().getApplicationContext().getSharedPreferences(Constants.SharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
                String oldId = oldPrefs.getString(Constants.SharedPreferences.INSTALLATION_ID, null);
                if (oldId != null) {
                    Props props = new Props();
                    props.put(AnalyticConstants.TOKEN, response.getToken());
                    props.put(AnalyticConstants.AIS_USER_ID, oldId);
                    LiveNationAnalytics.track(AnalyticConstants.MIGRATION_COMPLETED, AnalyticsCategory.HOUSEKEEPING, props);

                    SharedPreferences newPrefs = LiveNationApplication.get().getApplicationContext().getSharedPreferences(Constants.SharedPreferences.IAS_NAME, Context.MODE_PRIVATE);
                    newPrefs.edit().putString(Constants.SharedPreferences.IAS_USER_ID, oldId).commit();
                    oldPrefs.edit().clear().commit();

                    //Migration
                    LocalBroadcastManager.getInstance(LiveNationLibrary.getContext()).sendBroadcast(new Intent(com.livenation.mobile.android.platform.Constants.MIGRATION_UPDATE_INTENT_FILTER));

                }
                if (callback != null) {
                    callback.onResponse(response);
                }
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                if (callback != null) {
                    callback.onErrorResponse(error);
                }
            }
        });
    }

    @Override
    public void setAccessToken(AccessToken accessTokenValue) {
        super.setAccessToken(accessTokenValue);
        if (accessTokenValue != null) {
            //Apsalar
            Apsalar.event(AnalyticConstants.APSALAR_LN_LOGIN, AnalyticConstants.TOKEN, accessTokenValue.getToken(), AnalyticConstants.TOKEN_TYPE, accessTokenValue.getType());
        }
    }
}
