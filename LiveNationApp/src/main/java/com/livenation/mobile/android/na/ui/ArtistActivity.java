package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.artist_menu, menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            Toast.makeText(this, "Unimplemented", Toast.LENGTH_SHORT).show();

            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    //endregion
}
