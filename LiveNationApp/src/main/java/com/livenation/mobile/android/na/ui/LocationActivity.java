package com.livenation.mobile.android.na.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.CitySearchFragment;
import com.livenation.mobile.android.na.ui.fragments.LocationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

/**
 * Created by cchilton on 3/12/14.
 */

public class LocationActivity extends LiveNationFragmentActivity {
    private LocationFragment fragment;
    private final int REQUEST_CODE_CITY_SEARCH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(R.layout.actionbar_location_custom);
        fragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag("location");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home_search_item:
                Intent intent = new Intent(this, CitySearchActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CITY_SEARCH);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_CODE_CITY_SEARCH:
                City city = (City) data.getSerializableExtra(CitySearchFragment.DATA_RESULT_KEY);
                fragment.setCity(city);
                break;
            default:

        }
    }
}
