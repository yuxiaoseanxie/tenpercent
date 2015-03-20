package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.na.ui.fragments.CitySearchFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

import android.app.Activity;
import android.content.Intent;
import android.mobile.livenation.com.livenationui.activity.base.LiveNationFragmentActivity;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.analytics.Props;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by cchilton on 3/12/14.
 */

public class LocationActivity extends LiveNationFragmentActivity {
    private final int REQUEST_CODE_CITY_SEARCH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
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
                    Props props = new Props();
                    props.put(AnalyticConstants.LOCATION_NAME, city.getName());
                    props.put(AnalyticConstants.LOCATION_LATLONG, city.getLat() + "," + city.getLng());
                    LiveNationAnalytics.track(AnalyticConstants.SUBMIT_LOCATION_QUERY, AnalyticsCategory.LOCATION, props);
                    LiveNationApplication.getLocationProvider().addLocationHistory(city);
                    LiveNationApplication.getLocationProvider().setLocationMode(LocationManager.MODE_USER);
                }
                break;
            default:

        }
    }

    private void startCitySearchActivity() {
        Intent intent = new Intent(this, CitySearchActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CITY_SEARCH);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_LOCATION;
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_LOCATION;
    }
}
