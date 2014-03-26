package com.livenation.mobile.android.na.scan.aggregators;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.HashSet;
import java.util.Set;

public class DeviceArtistAggregator implements ArtistAggregator {

    Context context;

    public DeviceArtistAggregator(Context context) {
        this.context = context;
    }

    @Override
    public Set<String> getArtists() {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.ARTIST, "artist"}, null, null, null);

        Set<String> artistSet = new HashSet<String>();
        while (cursor.moveToNext()) {
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            artistSet.add(artist);
        }

        return artistSet;
    }
}
