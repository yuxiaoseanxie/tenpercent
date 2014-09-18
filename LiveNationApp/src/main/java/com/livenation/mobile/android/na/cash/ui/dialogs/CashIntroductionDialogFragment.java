package com.livenation.mobile.android.na.cash.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.ui.CashRecipientsActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.mobilitus.tm.tickets.models.Total;

public class CashIntroductionDialogFragment extends DialogFragment {
    public static final String TAG = CashIntroductionDialogFragment.class.getSimpleName();
    private static final String PREF_HAS_SHOWN_INTRO = "has_shown_intro";

    public static boolean shouldShow() {
        return !CashUtils.getPreferences().getBoolean(PREF_HAS_SHOWN_INTRO, false);
    }

    public static CashIntroductionDialogFragment newInstance(@NonNull Total total,
                                                             int ticketQuantity,
                                                             @NonNull Event event) {
        CashIntroductionDialogFragment dialogFragment = new CashIntroductionDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(CashUtils.EXTRA_TOTAL, total);
        arguments.putInt(CashUtils.EXTRA_TICKET_QUANTITY, ticketQuantity);
        arguments.putSerializable(CashUtils.EXTRA_EVENT, event);
        dialogFragment.setArguments(arguments);

        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cash_introduction);
        Button callToAction = (Button) dialog.findViewById(R.id.dialog_cash_introduction_button_cta);
        callToAction.setOnClickListener(callToActionClick);
        return dialog;
    }

    private final View.OnClickListener callToActionClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CashUtils.getPreferences().edit()
                    .putBoolean(PREF_HAS_SHOWN_INTRO, true)
                    .apply();

            dismiss();
        }
    };

}
