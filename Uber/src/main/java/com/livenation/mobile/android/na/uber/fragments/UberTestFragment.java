package com.livenation.mobile.android.na.uber.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.livenation.mobile.android.na.uber.R;
import com.livenation.mobile.android.na.uber.service.UberHelper;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberTestFragment extends Fragment implements UberHelper.UberDialogCallback {
    private static final float[] LOCATION_SF = {37.7833f, -122.4167f};
    private static final float[] LOCATION_EAST_BAY = {37.5423f, -122.04f};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uber_test, container, false);
        UberHelper.getUberDialogFragment(LOCATION_SF[0], LOCATION_SF[1], LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1], this);
        return view;
    }

    @Override
    public void onGetUberDialogComplete(DialogFragment dialog) {
        if (getActivity() == null) return;
        dialog.show(getFragmentManager(), "uber");
    }

    @Override
    public void onGetUberDialogError() {
        if (getActivity() == null) return;
        Toast.makeText(getActivity(), "Failed to retrieve the data for Uber dialog", Toast.LENGTH_SHORT).show();
    }
}
