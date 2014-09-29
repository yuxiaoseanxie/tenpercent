package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
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
import com.livenation.mobile.android.ticketing.utils.forms.ValidationManager;
import com.livenation.mobile.android.ticketing.utils.forms.listeners.EditTextValidationListener;
import com.livenation.mobile.android.ticketing.utils.forms.validators.NotEmptyValidator;

import rx.Observable;
import rx.Observer;

import static rx.android.observables.AndroidObservable.bindFragment;

public class CashOnboardingVerifyFragment extends CashOnboardingFragment {
    private static final long RESEND_ENABLE_DELAY = 2000;

    private EditText code;
    private Button resend;

    private ValidationManager validationManager;

    private boolean requestedCode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.validationManager = new ValidationManager();

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

        validationManager.attach(code, NotEmptyValidator.getInstance(), new EditTextValidationListener());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        validationManager.detach(code);
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

        Observable<CashResponse> observable = bindFragment(this, SquareCashService.getInstance().requestPhoneVerification(getPhoneNumber()));
        observable.subscribe(new Observer<CashResponse>() {
            @Override
            public void onCompleted() {
                loadingDialogFragment.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(e);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);

                resendEnabler.removeMessages(0);
                enableResend();
            }

            @Override
            public void onNext(CashResponse cashResponse) {
                scheduleEnableResend();
            }
        });
    }

    @Override
    public void next() {
        if (!validationManager.isValid())
            return;

        CashUtils.dismissKeyboard(code);

        final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        Observable<CashResponse> observable = bindFragment(this, SquareCashService.getInstance().verifyPhoneNumber(getPhoneNumber(), code.getText().toString()));
        observable.subscribe(new Observer<CashResponse>() {
            @Override
            public void onCompleted() {
                loadingDialogFragment.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(e);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onNext(CashResponse cashResponse) {
                getCashRequestDetailsActivity().continueToIdentity();
            }
        });
    }


    //region Listeners

    private final TextView.OnEditorActionListener codeEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_GO || (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                next();

                return true;
            }
            return false;
        }
    };

    private final View.OnClickListener resendCodeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(@NonNull View view) {
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
        public boolean handleMessage(@NonNull Message message) {
            enableResend();

            return true;
        }
    });

    //endregion
}
