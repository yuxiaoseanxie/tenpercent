/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.EventsPresenter;

public abstract class LiveNationFragment extends Fragment implements LiveNationFragmentContract {

    @Override
    public ImageLoader getImageLoader() {
        return LiveNationApplication.get().getImageLoader();
    }

    @Override
    public EventsPresenter getEventsPresenter() {
        return LiveNationApplication.get().getEventsPresenter();
    }

    @Override
    public ArtistEventsPresenter getArtistEventsPresenter() {
        return LiveNationApplication.get().getArtistEventsPresenter();
    }

    @Override
    public AccountPresenters getAccountPresenters() {
        return LiveNationApplication.get().getAccountPresenters();
    }

    public void addFragment(int containerId, Fragment fragment, String tag) {
        if (getActivity() == null) {
            Log.e("AddFragment", "Called after activity destroyed");
            if (!BuildConfig.DEBUG) {
                //don't allow app to crash in production builds
                return;
            }
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(containerId, fragment, tag);
        transaction.commitAllowingStateLoss();
    }

    public void removeFragment(Fragment fragment) {
        if (getActivity() == null) {
            Log.e("RemoveFragment", "Called after activity destroyed");
            if (!BuildConfig.DEBUG) {
                //don't allow app to crash in production builds
                return;
            }
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }
}
