package com.livenation.mobile.android.na.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.ShowsListNonScrollingFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;

public class ArtistEventsActivity extends Activity {
    public static final String EXTRA_EVENTS = "com.livenation.mobile.android.na.ui.ArtistEventsActivity.EXTRA_EVENTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_events);
    }
}
