/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.views.FavoritesView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

import java.util.List;


public class FavoriteActivity extends LiveNationFragmentActivity implements FavoritesView {
    private FavoritesView favoritesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);

        getActionBar().setCustomView(R.layout.actionbar_favorite_custom);
        favoritesView = (FavoritesView) getSupportFragmentManager().findFragmentById(R.id.activity_favorite_content);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        deinit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;
        }
        return true;
    }

    @Override
    public void setFavorites(List<Favorite> favorites) {
        if (favoritesView == null) {
            //TODO: Possible race condition?
            return;
        }
        favoritesView.setFavorites(favorites);
    }

    private void init() {
        getFavoritesPresenter().initialize(FavoriteActivity.this, getIntent().getExtras(), FavoriteActivity.this);
    }

    private void deinit() {
        getFavoritesPresenter().cancel(FavoriteActivity.this);
    }

    private FavoritesPresenter getFavoritesPresenter() {
        return LiveNationApplication.get().getFavoritesPresenter();
    }

    private void navigateUp() {
        Intent intent = new Intent(FavoriteActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
