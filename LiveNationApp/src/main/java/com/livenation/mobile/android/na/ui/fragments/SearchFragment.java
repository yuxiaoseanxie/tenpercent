package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.ui.support.LiveNationFragment;

/**
 * Created by cchilton on 4/2/14.
 */
public class SearchFragment extends LiveNationFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new View(getActivity());
    }
}
