package com.livenation.mobile.android.na.scan.aggregators;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.HashSet;
import java.util.Set;

/**
 * Aggregator used to get artist names from music songs stored on the SDCard
 */
public class DeviceArtistAggregator extends UriArtistAggregator {

    protected DeviceArtistAggregator(Context context) {
        super(context);
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }
}
