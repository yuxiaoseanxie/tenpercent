package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.mobilitus.tm.tickets.models.User;

public class CashOnboardingNameFragment extends CashOnboardingFragment {
    private EditText nameField;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_onboarding_name, container, false);

        this.nameField = (EditText) view.findViewById(R.id.fragment_cash_name_field);
        nameField.setOnEditorActionListener(nameEditorListener);

        if (Ticketing.getTicketService().hasSession() && Ticketing.getTicketService().getUser() != null) {
            String name = "";
            User user = Ticketing.getTicketService().getUser();
            if (!TextUtils.isEmpty(user.getFirstName())) {
                name += user.getFirstName();
            }

             if (!TextUtils.isEmpty(user.getLastName())) {
                 if (name.length() > 0)
                     name += " ";

                 name += user.getLastName();
             }

            nameField.setText(name);
        }

        return view;
    }

    //endregion


    @Override
    public void next() {
        SquareCashService.getInstance().updateUserFullName(nameField.getText().toString(), new SquareCashService.ApiCallback<CashResponse>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onResponse(CashResponse response) {
                getCashRequestDetailsActivity().setName(nameField.getText().toString());
                getCashRequestDetailsActivity().continueToPhoneVerification();
            }
        });
    }


    private final TextView.OnEditorActionListener nameEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                CashUtils.dismissKeyboard(textView);

                next();

                return true;
            }

            return false;
        }
    };
}
