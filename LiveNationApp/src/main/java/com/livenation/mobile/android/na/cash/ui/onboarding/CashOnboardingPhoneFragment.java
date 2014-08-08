package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnEditorAction;

public class CashOnboardingPhoneFragment extends CashOnboardingFragment {
    @InjectView(R.id.fragment_cash_onboarding_verify_number) EditText number;

    private final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();

    //region Lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_onboarding_phone, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    //endregion


    //region Handshake

    private void retrieveCustomerStatus() {
        SquareCashService.getInstance().retrieveCustomerStatus(new SquareCashService.ApiCallback<CashCustomerStatus>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onResponse(CashCustomerStatus response) {
                loadingDialogFragment.dismiss();
                getCashRequestDetailsActivity().continueWithCustomerStatus(response);
            }
        });
    }

    @Override
    public void next() {
        CashUtils.dismissKeyboard(number);

        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        SquareCashService.getInstance().startSession(null, number.getText().toString(), new SquareCashService.ApiCallback<CashSession>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onResponse(CashSession response) {
                getCashRequestDetailsActivity().setPhoneNumber(number.getText().toString());
                retrieveCustomerStatus();
            }
        });
    }

    //endregion


    @OnEditorAction(R.id.fragment_cash_onboarding_verify_number)
    public boolean onNumberEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            next();

            return true;
        }

        return false;
    }
}
