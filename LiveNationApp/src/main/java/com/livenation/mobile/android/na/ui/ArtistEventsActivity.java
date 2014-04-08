package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.ShowsListNonScrollingFragment;
import com.livenation.mobile.android.na.ui.views.DetailShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.ArrayList;

public class ArtistEventsActivity extends FragmentActivity {
    public static final String EXTRA_ARTIST = "com.livenation.mobile.android.na.ui.ArtistEventsActivity.EXTRA_ARTIST";
    public static final String EXTRA_EVENTS = "com.livenation.mobile.android.na.ui.ArtistEventsActivity.EXTRA_EVENTS";

    private ShowsListNonScrollingFragment shows;
    private Artist artist;
    private ArrayList<Event> events;

    //region Lifecycle

    public static Bundle getArguments(Artist artist, ArrayList<Event> events) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(EXTRA_ARTIST, artist);
        arguments.putSerializable(EXTRA_EVENTS, events);
        return arguments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_events);

        this.artist = (Artist) getIntent().getSerializableExtra(EXTRA_ARTIST);
        this.events = (ArrayList<Event>) getIntent().getSerializableExtra(EXTRA_EVENTS);

        this.shows = (ShowsListNonScrollingFragment) getSupportFragmentManager().findFragmentById(R.id.activity_artist_events_shows_fragment);
        shows.setDisplayMode(DetailShowView.DisplayMode.ARTIST);
        shows.setEvents(events);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(artist.getName());
    }

    //endregion
}
