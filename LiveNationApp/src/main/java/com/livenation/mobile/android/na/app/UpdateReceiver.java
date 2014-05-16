package com.livenation.mobile.android.na.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.livenation.mobile.android.na.helpers.MusicSyncHelper;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

/**
 * Created by elodieferrais on 4/17/14.
 */
public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(UpdateReceiver.class.getSimpleName(), "Package Updated, UpdateReceiver Called");

        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        Boolean isMusicScanAllowed = sharedPreferences.getBoolean(Constants.SharedPreferences.USER_ALLOWS_MEDIA_SCRAPE, false);

        if (isMusicScanAllowed) {
            LiveNationApplication.get().setIsMusicSync(true);
            MusicSyncHelper musicSyncHelper = new MusicSyncHelper();
            musicSyncHelper.syncMusic(context, new ApiService.BasicApiCallback<Void>() {
                @Override
                public void onResponse(Void response) {
                    sharedPreferences.edit().remove(Constants.SharedPreferences.USER_ALLOWS_MEDIA_SCRAPE);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    //Nothing to do
                }
            });
        }
    }
}
