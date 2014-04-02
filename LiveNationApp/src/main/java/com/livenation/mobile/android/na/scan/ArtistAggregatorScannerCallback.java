package com.livenation.mobile.android.na.scan;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;

/**
 * Interface used to give the result of the com.livenation.mobile.android.na.scan
 */
public interface ArtistAggregatorScannerCallback {
    void onSuccess(MusicLibrary musicLibrary);

    void onError(int errorCode, String message);
}
