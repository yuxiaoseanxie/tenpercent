package com.livenation.mobile.android.na.cash.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.NumberPicker;

import com.livenation.mobile.android.na.R;

public class CashQuantityDialogFragment extends DialogFragment implements NumberPicker.OnValueChangeListener {
    public static final String TAG = CashQuantityDialogFragment.class.getSimpleName();
    public static final String ARG_QUANTITY = "com.livenation.mobile.android.na.cash.CashQuantityDialogFragment.ARG_QUANTITY";
    public static final String ARG_VALUE = "com.livenation.mobile.android.na.cash.CashQuantityDialogFragment.ARG_VALUE";

    public static CashQuantityDialogFragment newInstance(int quantity, int value) {
        CashQuantityDialogFragment fragment = new CashQuantityDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(ARG_QUANTITY, quantity);
        arguments.putInt(ARG_VALUE, value);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        NumberPicker numberPicker = new NumberPicker(getActivity());
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(getQuantity());
        numberPicker.setValue(getValue());
        numberPicker.setOnValueChangedListener(this);
        builder.setView(numberPicker);

        builder.setTitle(R.string.cash_select_quantity);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Intent result = new Intent();
                result.putExtra(ARG_QUANTITY, getQuantity());
                result.putExtra(ARG_VALUE, getValue());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
            }
        });

        setCancelable(true);

        return builder.create();
    }


    public int getQuantity() {
        return getArguments().getInt(ARG_QUANTITY, 1);
    }

    public void setQuantity(int quantity) {
        getArguments().putInt(ARG_QUANTITY, quantity);
    }

    public int getValue() {
        return getArguments().getInt(ARG_VALUE, 1);
    }

    public void setValue(int value) {
        getArguments().putInt(ARG_VALUE, value);
    }


    @Override
    public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
        setValue(newValue);
    }
}
