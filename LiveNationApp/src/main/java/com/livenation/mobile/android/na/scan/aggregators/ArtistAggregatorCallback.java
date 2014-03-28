package com.livenation.mobile.android.na.scan.aggregators;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibraryEntry;

import java.util.List;

/**
 * Created by elodieferrais on 3/27/14.
 */
public interface ArtistAggregatorCallback {
    public void onResult(List<MusicLibraryEntry> libraryEntries);
}
