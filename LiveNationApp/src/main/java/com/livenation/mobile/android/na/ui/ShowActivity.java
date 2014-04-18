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
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.SingleEventView;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class ShowActivity extends DetailBaseFragmentActivity implements SingleEventView {

    private Event event;
    private SingleEventView singleEventView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        singleEventView = (SingleEventView) getSupportFragmentManager().findFragmentById(R.id.activity_show_content);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deinit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setEvent(Event event) {
        if (singleEventView == null) {
            //TODO: Possible race condition?
            return;
        }
        this.event = event;
        singleEventView.setEvent(event);
    }

    private void init() {
        getSingleEventPresenter().initialize(ShowActivity.this, getIntent().getExtras(), ShowActivity.this);
    }

    private void deinit() {
        getSingleEventPresenter().cancel(ShowActivity.this);
    }

    private SingleEventPresenter getSingleEventPresenter() {
        return LiveNationApplication.get().getSingleEventPresenter();
    }

    private void navigateUp() {
        Intent intent = new Intent(ShowActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //region Share Overrides

    @Override
    protected boolean isShareAvailable() {
        return (event != null);
    }

    @Override
    protected String getShareTitle() {
        return "Show";
    }

    @Override
    protected String getShareText() {
        return "Check out " + event.getName();
    }

    //endregion
}
