package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.livenation.mobile.android.na.R;

public class ArtistActivity extends FragmentActivity {
    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //endregion


    //region Menus

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    //endregion
}
