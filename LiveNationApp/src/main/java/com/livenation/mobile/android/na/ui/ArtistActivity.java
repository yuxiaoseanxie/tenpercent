package com.livenation.mobile.android.na.ui;

import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.ArtistFragment;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;

public class ArtistActivity extends DetailBaseFragmentActivity {
    private ArtistFragment artistFragment;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        artistFragment = (ArtistFragment) getSupportFragmentManager().findFragmentById(R.id.activity_artist_fragment);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //endregion


    //region Share Overrides

    @Override
    protected boolean isShareAvailable() {
        return (artistFragment.getArtist() != null);
    }

    @Override
    protected String getShareSubject() {
        return artistFragment.getArtist().getName();
    }

    @Override
    protected String getShareText() {
        Artist artist = artistFragment.getArtist();
        String artistTemplate = getString(R.string.share_template_artist);
        return artistTemplate.replace("$ARTIST", artist.getName())
                             .replace("$LINK", artist.getWebUrl());
    }

    //endregion
}
