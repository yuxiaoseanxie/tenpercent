package com.livenation.mobile.android.na.scan.aggregators;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibraryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class extended by aggregator class when the purpose of the aggregator is to gather artist name stored on the disk.
 * (Otherwise, use only {@link com.livenation.mobile.android.na.scan.aggregators.ArtistAggregator})
 */
public abstract class UriArtistAggregator implements ArtistAggregator {

    private Context context;

    public UriArtistAggregator(Context context) {
        this.context = context;
    }

    /**
     * @return a set of artist name. Never returns null, it returns an empty Set when the user does have any music
     * @throws Exception when the context or the uri is null
     */
    @Override
    public void getArtists(ArtistAggregatorCallback callback) {
        if (getUri() == null || context == null) {
            throw new NullPointerException("Make sure that the context and the uri is not null");
        }
        Cursor cursor = context.getContentResolver().query(getUri(), new String[]{MediaStore.Audio.Media.ARTIST}, null, null, null);

        List<MusicLibraryEntry> libraryEntries = new ArrayList<MusicLibraryEntry>();
        while (cursor != null && cursor.moveToNext()) {
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            MusicLibraryEntry musicLibraryEntry = new MusicLibraryEntry(artist);
            //TODO see if we can get more information (playCount, totalSong ...)
            libraryEntries.add(musicLibraryEntry);
            //TODO can be useful to filter a little bit the output. (Sometime <unknown> is an answer for example)
        }
        cursor.close();
        callback.onResult(libraryEntries);
    }

    protected abstract Uri getUri();
}
