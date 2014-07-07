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
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
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
                    sendGrantedAccesToMusicLibraryAnalytics(apiService.getApiConfig().getAccessToken(), isMusicScanAllowed);
                }

                @Override
                public void onApiServiceNotAvailable() {
                    sendGrantedAccesToMusicLibraryAnalytics(null, isMusicScanAllowed);
                }
            });
            MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
            musicSyncHelper.syncMusic(context, new ApiService.BasicApiCallback<Void>() {
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

    private void sendGrantedAccesToMusicLibraryAnalytics(String token, boolean isMusicScanAllowed) {

        Props props = new Props();
        if (token != null) {
            props.put(AnalyticConstants.ANDROID_DEVICE_ID, token);
        }
        props.put(AnalyticConstants.GRANTED_ACCESS_TO_MUSIC_LIBRARY, isMusicScanAllowed);
        LiveNationAnalytics.track(AnalyticConstants.GRANTED_ACCESS_TO_MUSIC, AnalyticsCategory.HOUSEKEEPING, props);
    }
}
