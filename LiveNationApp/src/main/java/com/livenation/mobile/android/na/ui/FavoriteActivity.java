/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;

import android.content.Intent;
import android.mobile.livenation.com.livenationui.activity.base.LiveNationFragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class FavoriteActivity extends LiveNationFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_favorite);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home_search_item:
                Intent intent = new Intent(this, FavoriteSearchActivity.class);
                intent.putExtra(FavoriteSearchActivity.EXTRA_KEY_SEARCH_MODE, FavoriteSearchActivity.EXTRA_VALUE_SEARCH_MODE_ARTIST_VENUES);
                intent.putExtra(FavoriteSearchActivity.EXTRA_KEY_ON_CLICK_ACTION, FavoriteSearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_FAVORITE);

                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_FAVORITES;
    }


    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_FAVORITES;
    }
}
