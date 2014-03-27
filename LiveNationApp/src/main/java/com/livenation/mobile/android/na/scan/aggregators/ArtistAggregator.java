package com.livenation.mobile.android.na.scan.aggregators;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryEntry;

import java.util.List;

/**
 * ArtistAggregator should be implemented by all classes which aggregate artist in order to be used
 * in the {@link com.livenation.mobile.android.na.scan.ArtistAggregatorScanner}
 */
public interface ArtistAggregator {

    /**
     * @return a set of artist name
     */
    public List<LibraryEntry> getArtists();
}
