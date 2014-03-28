package com.livenation.mobile.android.na.scan.aggregators;

/**
 * ArtistAggregator should be implemented by all classes which aggregate artist in order to be used
 * in the {@link com.livenation.mobile.android.na.scan.ArtistAggregatorScanner}
 */
public interface ArtistAggregator{

    /**
     * @return a set of artist name
     */
    public void getArtists(ArtistAggregatorCallback callback);
}
