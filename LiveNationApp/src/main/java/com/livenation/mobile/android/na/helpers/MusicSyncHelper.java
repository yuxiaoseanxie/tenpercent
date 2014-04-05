package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;

/**
 * Created by elodieferrais on 4/4/14.
 */
public class MusicSyncHelper {

    public void syncMusic(Context context) {
        final boolean isToastShowable = isToastShowable(context);
        if (isToastShowable) {
            Toast.makeText(context, "Music Scan started", Toast.LENGTH_SHORT).show();
        }
        final Toast successToast = Toast.makeText(context, "Music Scan done! ", Toast.LENGTH_SHORT);
        final Toast failToast = Toast.makeText(context, "Music Scan has failed! ", Toast.LENGTH_SHORT);
        MusicLibraryScannerHelper musicLibraryScannerHelper = new MusicLibraryScannerHelper();
        musicLibraryScannerHelper.getMusicDiffSinceLastSync(context, new ApiService.BasicApiCallback<MusicLibrary>() {
            @Override
            public void onSuccess(MusicLibrary result) {
                if (isToastShowable) {
                    successToast.setText("Music Scan done! " + String.valueOf(result.getData().size()) + " artist has been synchronyzed");
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

    private boolean isToastShowable(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.DEBUG_MODE_DATA, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constants.SharedPreferences.DEBUG_MODE_IS_DEBUG_MODE_ACTIVATED, false);
    }
}
