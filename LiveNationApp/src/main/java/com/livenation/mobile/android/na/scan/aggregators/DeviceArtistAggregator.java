package com.livenation.mobile.android.na.scan.aggregators;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Aggregator used to get artist names from music songs stored on the SDCard
 */
public class DeviceArtistAggregator extends UriArtistAggregator {

    public DeviceArtistAggregator(Context context) {
        super(context);
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }
}
