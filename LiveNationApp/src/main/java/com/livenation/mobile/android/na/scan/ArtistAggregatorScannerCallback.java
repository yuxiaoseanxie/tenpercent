package com.livenation.mobile.android.na.scan;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryDump;

/**
 * Interface used to give the result of the scan
 */
public interface ArtistAggregatorScannerCallback {
    void onSuccess(LibraryDump libraryDump);

    void onError(int errorCode, String message);
}
