package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.mobilitus.tm.tickets.models.User;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnEditorAction;

public class CashOnboardingNameFragment extends Fragment {
    @InjectView(R.id.fragment_cash_name_field) EditText nameField;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_onboarding_name, container, false);
        ButterKnife.inject(this, view);

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


    private CashOnboardingActivity getCashRequestDetailsActivity() {
        return (CashOnboardingActivity) getActivity();
    }


    public void updateName() {
        getCashRequestDetailsActivity().setName(nameField.getText().toString());
        getCashRequestDetailsActivity().continueToPhoneVerification();
    }


    @OnEditorAction(R.id.fragment_cash_name_field)
    public boolean onNameEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            CashUtils.dismissKeyboard(textView);

            updateName();

            return true;
        }

        return false;
    }
}
