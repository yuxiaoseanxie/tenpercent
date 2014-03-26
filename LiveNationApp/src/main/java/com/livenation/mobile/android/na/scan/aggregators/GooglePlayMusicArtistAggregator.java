package com.livenation.mobile.android.na.scan.aggregators;

import android.content.Context;
import android.net.Uri;

/**
 * Aggregator used to get artist names from google play account used on the same device which runs the current app
 */
public class GooglePlayMusicArtistAggregator extends UriArtistAggregator {

    protected GooglePlayMusicArtistAggregator(Context context) {
        super(context);
    }

    @Override
    protected Uri getUri() {
        return Uri.parse("content://com.google.android.music.MusicContent/audio");
    }
}
