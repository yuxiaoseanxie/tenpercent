package com.livenation.mobile.android.na.scan.aggregators;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibraryEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Aggregator used to get artist names from google play account used on the same device which runs the current app
 */
public class GooglePlayMusicArtistAggregator implements ArtistAggregator {

    private Context context;

    public GooglePlayMusicArtistAggregator(Context context) {
        this.context = context;
    }


    /**
     * @return a set of artist name. Never returns null, it returns an empty Set when the user does have any music
     * @throws Exception when the context or the uri is null
     */
    @Override
    public void getArtists(Date sinceDate, ArtistAggregatorCallback callback) throws Exception {
        if (context == null) {
            throw new NullPointerException("Make sure that the context is not null");
        }
        String filter = null;
        if (sinceDate != null) {
            filter = "FileDate>=" + sinceDate.getTime();
        }

        Cursor cursorDiff = context.getContentResolver().query(Uri.parse("content://com.google.android.music.MusicContent/audio"), new String[]{MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST}, filter, null, null);

        //Save artist already processed
        Set<Integer> artistIds = new HashSet<Integer>();
        List<MusicLibraryEntry> libraryEntries = new ArrayList<MusicLibraryEntry>();
        MusicLibraryEntry musicLibraryEntry = null;
        while (cursorDiff != null && cursorDiff.moveToNext()) {

            //Save the artist as processed to avoid duplicates.
            Integer artistId = cursorDiff.getInt(cursorDiff.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            if (artistIds.contains(artistId)) {
                continue;
            }
            artistIds.add(artistId);

            //Create a basic MusicLibraryEntry
            String artist = cursorDiff.getString(cursorDiff.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            musicLibraryEntry = new MusicLibraryEntry(artist);


            //Get number of tracks of this artist
            Cursor cursorTracksCount = context.getContentResolver().query(Uri.parse("content://com.google.android.music.MusicContent/audio"), new String[]{MediaStore.Audio.Media.ARTIST_ID}, MediaStore.Audio.Media.ARTIST_ID + "=" + artistId, null, null);
            if (cursorTracksCount != null && cursorTracksCount.moveToNext()) {
                Integer numberOfTracks = cursorTracksCount.getCount();
                musicLibraryEntry.setTotalSongs(numberOfTracks);
            }

            cursorTracksCount.close();

            libraryEntries.add(musicLibraryEntry);
        }

        if (cursorDiff != null) {
            cursorDiff.close();
        }

        callback.onResult(libraryEntries);
    }
}
