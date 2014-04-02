package com.livenation.mobile.android.na.scan.aggregators;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibraryEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Aggregator used to get artist names from music songs stored on the SDCard
 */
public class DeviceArtistAggregator implements ArtistAggregator {

    private Context context;

    public DeviceArtistAggregator(Context context) {
        this.context = context;
    }

    /**
     * @return a set of artist name. Never returns null, it returns an empty Set when the user does have any music
     * @throws Exception when the context or the uri is null
     */
    @Override
    public void getArtists(Date sinceDate, ArtistAggregatorCallback callback) {
        if (context == null) {
            throw new NullPointerException("Make sure that the context is not null");
        }
        String filter = null;
        if (sinceDate != null) {
            filter = MediaStore.Audio.Media.DATE_ADDED + ">=" + sinceDate.getTime()/1000;
        }

        Cursor cursorDiff = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{ MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST}, filter, null, null);

        //Save artist already processed
        Set<Integer> artistIds = new HashSet<Integer>();
        List<MusicLibraryEntry> libraryEntries = new ArrayList<MusicLibraryEntry>();
        MusicLibraryEntry musicLibraryEntry = null;
        while (cursorDiff != null && cursorDiff.moveToNext()) {
            String artist = cursorDiff.getString(cursorDiff.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            if ("<unknown>".equals(artist)) {
                continue;
            }
            //Create a basic MusicLibraryEntry
            musicLibraryEntry = new MusicLibraryEntry(artist);

            //Save the artist as processed to avoid duplicates.
            Integer artistId = cursorDiff.getInt(cursorDiff.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            if (artistIds.contains(artistId)) {
                continue;
            }
            artistIds.add(artistId);

            //Get number of tracks of this artist
            Cursor cursorTracksCount = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.ARTIST_ID, "COUNT("+MediaStore.Audio.Media.TRACK+") AS number_of_track"}, MediaStore.Audio.Media.ARTIST_ID + "=" + artistId, null, null);
            if (cursorTracksCount != null && cursorTracksCount.moveToNext()) {
                Integer numberOfTracks = cursorTracksCount.getInt(cursorTracksCount.getColumnIndex("number_of_track"));
                musicLibraryEntry.setTotalSongs(numberOfTracks);
            }
            cursorTracksCount.close();

            libraryEntries.add(musicLibraryEntry);
        }

        cursorDiff.close();

        callback.onResult(libraryEntries);
    }
}
