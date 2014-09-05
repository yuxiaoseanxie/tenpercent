/*
 * 
 * @author Charlie Chilton 2014/01/17
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.EventsPresenter;

public abstract class LiveNationListFragment extends ListFragment implements LiveNationFragmentContract {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public ImageLoader getImageLoader() {
        return LiveNationApplication.get().getImageLoader();
    }

    @Override
    public EventsPresenter getEventsPresenter() {
        return LiveNationApplication.get().getEventsPresenter();
    }

    @Override
    public AccountPresenters getAccountPresenters() {
        return LiveNationApplication.get().getAccountPresenters();
    }

}
