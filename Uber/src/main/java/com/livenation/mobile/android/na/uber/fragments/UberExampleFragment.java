package com.livenation.mobile.android.na.uber.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.livenation.mobile.android.na.uber.R;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.UberHelper;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberExampleFragment extends Fragment implements UberHelper.UberDialogCallback {
    private static final float[] LOCATION_SF = {37.7833f, -122.4167f};
    private static final float[] LOCATION_EAST_BAY = {37.5423f, -122.04f};
    private static final int DIALOG_SELECT_UBER_RESULT = 1;

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
        dialog.setTargetFragment(this, DIALOG_SELECT_UBER_RESULT);
        dialog.show(getFragmentManager(), "uber");
    }

    @Override
    public void onGetUberDialogError() {
        if (getActivity() == null) return;
        Toast.makeText(getActivity(), "Failed to retrieve the data for Uber dialog", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case DIALOG_SELECT_UBER_RESULT:
                LiveNationEstimate estimate = (LiveNationEstimate) data.getExtras().getSerializable(UberDialogFragment.EXTRA_RESULT_ESTIMATE);
                Toast.makeText(getActivity(), "Estimate selected: " + estimate.getProduct().getDisplayName(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
