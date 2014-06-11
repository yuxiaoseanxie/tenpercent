package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.LibraryAffinitiesParameters;
import com.livenation.mobile.android.platform.api.transport.error.ErrorDictionary;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.Calendar;

import io.segment.android.models.Props;

/**
 * Created by elodieferrais on 4/4/14.
 */
public class MusicSyncHelper implements ApiServiceBinder {
    private MusicLibrary musicLibrary;
    private boolean isToastShowable;
    private Toast successToast;
    private Toast failToast;
    private Context context;
    private ApiService.BasicApiCallback<Void> callback;

    public void syncMusic(Context ctx) {
        this.callback = null;
        syncMusic(ctx, null);
    }

    public synchronized void syncMusic(Context ctx, ApiService.BasicApiCallback<Void> responseCallback) {
        this.context = ctx.getApplicationContext();
        this.callback = responseCallback;
        isToastShowable = isToastShowable(context);
        if (isToastShowable) {
            Toast.makeText(context, "Music Scan started", Toast.LENGTH_SHORT).show();
        }
        successToast = Toast.makeText(context, "Music Scan done! ", Toast.LENGTH_SHORT);
        failToast = Toast.makeText(context, "Music Scan has failed! ", Toast.LENGTH_SHORT);
        MusicLibraryScannerHelper musicLibraryScannerHelper = new MusicLibraryScannerHelper();
        musicLibraryScannerHelper.getMusicDiffSinceLastSync(context, new ApiService.BasicApiCallback<MusicLibrary>() {
            @Override
            public void onResponse(MusicLibrary result) {
                musicLibrary = result;
                if (result.getData().size() > 0) {
                    LiveNationApplication.get().getConfigManager().bindApi(MusicSyncHelper.this);
                } else {
                    if (isToastShowable) {
                        successToast.setText("Music Scan done! 0 artist has been synchronyzed");
                        successToast.show();
                    }
                }
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                if (isToastShowable) {
                    failToast.show();
                }
                if (callback != null) {
                    callback.onErrorResponse(error);
                    callback = null;
                }
            }
        });
    }

    private boolean isToastShowable(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.DEBUG_MODE_DATA, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SharedPreferences.DEBUG_MODE_IS_DEBUG_MODE_ACTIVATED, false);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        apiService.sendLibraryAffinities(new LibraryAffinitiesParameters().setLibraryDump(musicLibrary), new ApiService.BasicApiCallback<Void>() {
            @Override
            public void onResponse(Void result) {
                Props props = new Props();
                props.put(AnalyticConstants.NUMBER_OF_ARTISTS_FOUND, musicLibrary.getData().size());
                LiveNationAnalytics.track(AnalyticConstants.AFFINITY_MUSIC_LIBRARY_SCAN_COMPLETED, AnalyticsCategory.HOUSEKEEPING, props);

                SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SharedPreferences.MUSIC_SYNC_NAME, Context.MODE_PRIVATE).edit();
                editor.putLong(Constants.SharedPreferences.MUSIC_SYNC_LAST_SYNC_DATE_KEY, Calendar.getInstance().getTimeInMillis()).commit();
                if (isToastShowable) {
                    successToast.setText("Music Scan done! " + String.valueOf(musicLibrary.getData().size()) + " artist(s) has been synchronyzed");
                    successToast.show();
                }

                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.BroadCastReceiver.MUSIC_LIBRARY_UPDATE));
                if (callback != null) {
                    callback.onResponse(null);
                }
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                if (isToastShowable) {
                    failToast.show();
                }
                if (callback != null) {
                    callback.onErrorResponse(error);
                }
            }
        });
    }

    @Override
    public void onApiServiceNotAvailable() {
        if (callback != null) {
            callback.onErrorResponse(new LiveNationError(ErrorDictionary.ERROR_CODE_API_SERVICE_NOT_AVAILABLE));
        }
    }
}
