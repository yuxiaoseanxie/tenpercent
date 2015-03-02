/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import com.livenation.mobile.android.na.BuildConfig;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public abstract class LiveNationFragment extends Fragment {


    public void addFragment(int containerId, Fragment fragment, String tag) {
        if (getChildFragmentManager().findFragmentByTag(tag) != null) {
            return;
        }
        if (getActivity() == null) {
            Log.e("AddFragment", "Called after activity destroyed");
            if (!BuildConfig.DEBUG) {
                //don't allow app to crash in production builds
                return;
            }
        }
        getChildFragmentManager().beginTransaction().add(containerId, fragment, tag).commitAllowingStateLoss();
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
