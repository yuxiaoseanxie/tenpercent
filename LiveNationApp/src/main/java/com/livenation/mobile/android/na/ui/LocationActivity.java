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
                startCitySearchActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CITY_SEARCH:
                if (resultCode == Activity.RESULT_OK) {
                    City city = (City) data.getSerializableExtra(CitySearchFragment.DATA_RESULT_KEY);
                    fragment.setConfiguredLocation(city);
                }
                break;
            default:

        }
    }

    private void startCitySearchActivity() {
        Intent intent = new Intent(this, CitySearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CITY_SEARCH);
    }
}
