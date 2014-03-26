package com.livenation.mobile.android.na.scan.aggregators;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.HashSet;
import java.util.Set;

public class GooglePlayMusicArtistAggregator implements ArtistAggregator {

    Context context;

    public GooglePlayMusicArtistAggregator(Context context) {
        this.context = context;
    }

    @Override
    public Set<String> getArtists() {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://com.google.android.music.MusicContent/audio"),
                new String[] {"artist"}, null, null, null);

        Set<String> artistSet = new HashSet<String>();
        while (cursor.moveToNext()) {
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            artistSet.add(artist);
        }

        return artistSet;
    }
}
