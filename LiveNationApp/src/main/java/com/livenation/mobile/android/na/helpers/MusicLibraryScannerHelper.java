package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.scan.ArtistAggregatorScanner;
import com.livenation.mobile.android.na.scan.ArtistAggregatorScannerCallback;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by elodieferrais on 3/31/14.
 */
public class MusicLibraryScannerHelper {
    private Date sinceDate = null;
    public static int artistNumber = -1;

    public void getMusicDiffSinceLastSync(final Context context, final ApiService.BasicApiCallback<MusicLibrary> callback) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.MUSIC_SYNC_NAME, Context.MODE_PRIVATE);
        Long sinceDateLong =  sharedPreferences.getLong(Constants.SharedPreferences.MUSIC_SYNC_LAST_SYNC_DATE_KEY, -1l);

        if (sinceDateLong != -1l) {
            sinceDate = new Date(sinceDateLong);
        } else {
            sinceDate = null;
        }

        ArtistAggregatorScanner scanner = new ArtistAggregatorScanner();
        scanner.aggregate(context, new ArtistAggregatorScannerCallback() {
            @Override
            public void onSuccess(MusicLibrary musicLibrary) {
                callback.onSuccess(musicLibrary);
                artistNumber = musicLibrary.getData().size();
            }

            @Override
            public void onError(int errorCode, String message) {
                callback.onFailure(errorCode, message);
            }
        }, sinceDate);
    }
}
