package com.livenation.mobile.android.na.helpers;

import android.content.Context;

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
public class MusicSyncHelper {
    private Date sinceDate = null;

    public void getMusicDiffSinceLastSync(final Context context, final ApiService.BasicApiCallback<MusicLibrary> callback) {
        final PreferencePersistence preferencePersistence = new PreferencePersistence(Constants.SharedPreferences.MUSIC_SYNC_NAME);
        Long sinceDateLong = (Long) preferencePersistence.read(Constants.SharedPreferences.MUSIC_SYNC_LAST_SYNC_DATE_KEY, context);
        if (sinceDateLong != null) {
            sinceDate = new Date(sinceDateLong);
        }

        ArtistAggregatorScanner scanner = new ArtistAggregatorScanner();

        scanner.aggregate(context, new ArtistAggregatorScannerCallback() {
            @Override
            public void onSuccess(MusicLibrary musicLibrary) {
                preferencePersistence.write(Constants.SharedPreferences.MUSIC_SYNC_LAST_SYNC_DATE_KEY, Calendar.getInstance().getTimeInMillis(), context);
                callback.onSuccess(musicLibrary);
            }

            @Override
            public void onError(int errorCode, String message) {
                callback.onFailure(errorCode, message);
            }
        }, sinceDate);
    }
}
