package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;

/**
 * Created by elodieferrais on 4/4/14.
 */
public class MusicSyncHelper implements ApiServiceBinder{
    private MusicLibrary  musicLibrary;
    private boolean isToastShowable;
    private Toast successToast;
    private Toast failToast;

    public void syncMusic(Context context) {
        isToastShowable = isToastShowable(context);
        if (isToastShowable) {
            Toast.makeText(context, "Music Scan started", Toast.LENGTH_SHORT).show();
        }
        successToast = Toast.makeText(context, "Music Scan done! ", Toast.LENGTH_SHORT);
        failToast = Toast.makeText(context, "Music Scan has failed! ", Toast.LENGTH_SHORT);
        MusicLibraryScannerHelper musicLibraryScannerHelper = new MusicLibraryScannerHelper();
        musicLibraryScannerHelper.getMusicDiffSinceLastSync(context, new ApiService.BasicApiCallback<MusicLibrary>() {
            @Override
            public void onSuccess(MusicLibrary result) {
                musicLibrary = result;
                LiveNationApplication.get().getApiHelper().bindApi(MusicSyncHelper.this);
            }

            @Override
            public void onFailure(int errorCode, String message) {
                if (isToastShowable) {
                    failToast.show();
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
        apiService.sendLibraryAffinities(ApiParameters.createLibraryAffinitiesParameters().setLibraryDump(musicLibrary), new LiveNationApiService.SendLibraryAffinitiesCallback() {
            @Override
            public void onSuccess() {
                if (isToastShowable) {
                    successToast.setText("Music Scan done! " + String.valueOf(musicLibrary.getData().size()) + " artist has been synchronyzed");
                    successToast.show();
                }
            }

            @Override
            public void onFailure(int errorCode, String message) {
                if (isToastShowable) {
                    failToast.show();
                }
            }
        });
    }
}
