package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.PhoneNumberUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.ticketing.utils.OnThrottledClickListener;

public class CashOnboardingVerifyFragment extends CashOnboardingFragment {
    private static final long RESEND_ENABLE_DELAY = 2000;

    private EditText code;
    private Button resend;

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

        this.resend = (Button) view.findViewById(R.id.fragment_cash_onboarding_verify_resend);
        resend.setOnClickListener(resendCodeClickListener);
        resend.setVisibility(View.INVISIBLE);

        TextView title = (TextView) view.findViewById(R.id.fragment_cash_onboarding_verify_title);
        String formattedPhoneNumber = PhoneNumberUtils.formatNumber(getPhoneNumber());
        title.setText(getString(R.string.cash_verification_code_text_fmt, formattedPhoneNumber));

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

        scheduleEnableResend();

        final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        SquareCashService.getInstance().requestPhoneVerification(getPhoneNumber(), new SquareCashService.ApiCallback<CashResponse>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);

                resendEnabler.removeMessages(0);
                enableResend();
            }

            @Override
            public void onResponse(CashResponse response) {
                loadingDialogFragment.dismiss();

                scheduleEnableResend();
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


    //region Listeners

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

    private final View.OnClickListener resendCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            requestedCode = false;
            requestCode();

            resend.setText(R.string.cash_resend_verification_code_again);
            resend.setEnabled(false);

            scheduleEnableResend();
        }
    };

    //endregion


    //region Managing Resend State

    private void scheduleEnableResend() {
        resendEnabler.removeMessages(0);
        resendEnabler.sendEmptyMessageDelayed(0, RESEND_ENABLE_DELAY);
    }

    private void enableResend() {
        resend.setEnabled(true);
        resend.setVisibility(View.VISIBLE);
    }

    private final Handler resendEnabler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            enableResend();

            return true;
        }
    });

    //endregion
}
