package com.livenation.mobile.android.na.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;
import com.livenation.mobile.android.platform.init.provider.ProviderManager;
import com.livenation.mobile.android.platform.api.proxy.LiveNationConfig;
import com.segment.android.models.Props;

/**
 * Created by elodieferrais on 4/17/14.
 */
public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(UpdateReceiver.class.getSimpleName(), "Package Updated, UpdateReceiver Called");

        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        final Boolean isMusicScanAllowed = sharedPreferences.getBoolean(Constants.SharedPreferences.USER_ALLOWS_MEDIA_SCRAPE, false);

        if (isMusicScanAllowed) {
            LiveNationApplication.get().getConfigManager().bindApi(new ApiServiceBinder() {
                @Override
                public void onApiServiceAttached(LiveNationApiService apiService) {
                    sendGrantedAccessToMusicLibraryAnalytics(isMusicScanAllowed);
                }

                @Override
                public void onApiServiceNotAvailable() {
                    sendGrantedAccessToMusicLibraryAnalytics(isMusicScanAllowed);
                }
            });
            MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
            musicSyncHelper.syncMusic(context, new BasicApiCallback<Void>() {
                @Override
                public void onResponse(Void response) {
                    sharedPreferences.edit().remove(Constants.SharedPreferences.USER_ALLOWS_MEDIA_SCRAPE);
                    LiveNationApplication.get().setIsMusicSync(true);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    //Nothing to do
                }
            });
        }
    }

    private void sendGrantedAccessToMusicLibraryAnalytics(final boolean isMusicScanAllowed) {

        ProviderManager providerManager = new ProviderManager();
        providerManager.getConfigReadyFor(new ConfigCallback() {
            @Override
            public void onResponse(LiveNationConfig response) {
                Props props = new Props();
                props.put(AnalyticConstants.ANDROID_DEVICE_ID, response.getAccessToken().getToken());
                props.put(AnalyticConstants.GRANTED_ACCESS_TO_MUSIC_LIBRARY, isMusicScanAllowed);
                LiveNationAnalytics.track(AnalyticConstants.GRANTED_ACCESS_TO_MUSIC, AnalyticsCategory.HOUSEKEEPING, props);
            }

            @Override
            public void onErrorResponse(int errorCode) {
                Props props = new Props();
                props.put(AnalyticConstants.ANDROID_DEVICE_ID, "Unknown");
                props.put(AnalyticConstants.GRANTED_ACCESS_TO_MUSIC_LIBRARY, isMusicScanAllowed);
                LiveNationAnalytics.track(AnalyticConstants.GRANTED_ACCESS_TO_MUSIC, AnalyticsCategory.HOUSEKEEPING, props);

            }
        }, ProviderManager.ProviderType.ACCESS_TOKEN);
    }
}
