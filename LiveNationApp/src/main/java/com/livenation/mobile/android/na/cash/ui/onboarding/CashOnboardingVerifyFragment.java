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
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;

public class CashOnboardingVerifyFragment extends CashOnboardingFragment {
    private EditText code;

    private boolean requestedCode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_onboarding_verify, container, false);

        this.code = (EditText) view.findViewById(R.id.fragment_cash_onboarding_verify_code);
        code.setOnEditorActionListener(codeEditorListener);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        requestCode();
    }


    private String getPhoneNumber() {
        return getCashRequestDetailsActivity().getPhoneNumber();
    }


    private void requestCode() {
        if (requestedCode)
            return;

        requestedCode = true;

        final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        SquareCashService.getInstance().requestPhoneVerification(getPhoneNumber(), new SquareCashService.ApiCallback<CashResponse>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onResponse(CashResponse response) {
                loadingDialogFragment.dismiss();
            }
        });
    }

    @Override
    public void next() {
        CashUtils.dismissKeyboard(code);

        final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        SquareCashService.getInstance().verifyPhoneNumber(getPhoneNumber(), code.getText().toString(), new SquareCashService.ApiCallback<CashResponse>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onResponse(CashResponse response) {
                loadingDialogFragment.dismiss();
                getCashRequestDetailsActivity().setupCompleted();
            }
        });
    }


    private final TextView.OnEditorActionListener codeEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                next();

                return true;
            }
            return false;
        }
    };
}
