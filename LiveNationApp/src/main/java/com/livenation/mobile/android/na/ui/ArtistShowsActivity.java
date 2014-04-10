package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.ArtistShowsListFragment;
import com.livenation.mobile.android.na.ui.fragments.ShowsListNonScrollingFragment;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.ArrayList;

public class ArtistShowsActivity extends FragmentActivity {
    public static final String EXTRA_ARTIST_ID = "com.livenation.mobile.android.na.ui.ArtistShowsActivity.EXTRA_ARTIST_ID";
    public static final String EXTRA_ARTIST_NAME = "com.livenation.mobile.android.na.ui.ArtistShowsActivity.EXTRA_ARTIST_NAME";

    private ArtistShowsListFragment shows;

    //region Lifecycle

    public static Bundle getArguments(Artist artist) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(EXTRA_ARTIST_ID, artist.getId());
        arguments.putSerializable(EXTRA_ARTIST_NAME, artist.getName());
        return arguments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_shows);

        this.shows = (ArtistShowsListFragment) getSupportFragmentManager().findFragmentById(R.id.activity_artist_events_shows_fragment);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        String artistName = getIntent().getStringExtra(ArtistShowsActivity.EXTRA_ARTIST_NAME);
        getActionBar().setTitle(artistName);
    }

    //endregion
}
