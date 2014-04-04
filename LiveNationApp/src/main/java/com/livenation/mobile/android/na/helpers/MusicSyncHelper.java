package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.widget.Toast;

import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;

/**
 * Created by elodieferrais on 4/4/14.
 */
public class MusicSyncHelper {

    public void syncMusic(Context context) {
        Toast.makeText(context, "Music Scan started", Toast.LENGTH_SHORT).show();
        final Toast successToast = Toast.makeText(context, "Music Scan done! ", Toast.LENGTH_SHORT);
        final Toast failToast = Toast.makeText(context, "Music Scan has failed! ", Toast.LENGTH_SHORT);
        MusicLibraryScannerHelper musicLibraryScannerHelper = new MusicLibraryScannerHelper();
        musicLibraryScannerHelper.getMusicDiffSinceLastSync(context, new ApiService.BasicApiCallback<MusicLibrary>() {
            @Override
            public void onSuccess(MusicLibrary result) {
                successToast.setText("Music Scan done! " + String.valueOf(result.getData().size()) + " artist has been synchronyzed");
                successToast.show();
            }

            @Override
            public void onFailure(int errorCode, String message) {
                failToast.show();
            }
        });
    }
}
