package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by elodieferrais on 2/23/15.
 */
public class ShowTipsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_showtips, container,false);

        LineUpFragment lineUpFragment = LineUpFragment.newInstance(new ArrayList<Artist>());
        getChildFragmentManager().beginTransaction().add(R.id.lineup_container, lineUpFragment).commit();

        return view;
    }
}
