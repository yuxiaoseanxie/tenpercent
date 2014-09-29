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
import com.livenation.mobile.android.ticketing.utils.forms.ValidationManager;
import com.livenation.mobile.android.ticketing.utils.forms.listeners.EditTextValidationListener;
import com.livenation.mobile.android.ticketing.utils.forms.validators.NotEmptyValidator;

import rx.Observable;
import rx.Observer;

import static rx.android.observables.AndroidObservable.bindFragment;

public class CashOnboardingPhoneFragment extends CashOnboardingFragment {
    private EditText number;

    private ValidationManager validationManager;

    private final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();

    //region Lifecycle


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.validationManager = new ValidationManager();

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_onboarding_phone, container, false);

        this.number = (EditText) view.findViewById(R.id.fragment_cash_onboarding_verify_number);
        number.setOnEditorActionListener(numberEditorListener);
        number.setText(SquareCashService.getInstance().getStoredPhoneNumber());

        validationManager.attach(number, NotEmptyValidator.getInstance(), new EditTextValidationListener());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        validationManager.detach(number);
    }

    //endregion


    //region Handshake

    private void retrieveCustomerStatus() {
        Observable<CashCustomerStatus> observable = bindFragment(this, SquareCashService.getInstance().retrieveCustomerStatus());
        observable.subscribe(new Observer<CashCustomerStatus>() {
            @Override
            public void onCompleted() {
                loadingDialogFragment.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance((VolleyError) e);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onNext(CashCustomerStatus customerStatus) {
                getCashRequestDetailsActivity().continueWithCustomerStatus(customerStatus);
            }
        });
    }

    @Override
    public void next() {
        if (!validationManager.isValid())
            return;

        CashUtils.dismissKeyboard(number);

        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        Observable<CashSession> observable = bindFragment(CashOnboardingPhoneFragment.this, SquareCashService.getInstance().startSession(null, number.getText().toString()));
        observable.subscribe(new Observer<CashSession>() {
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
            public void onNext(CashSession session) {
                getCashRequestDetailsActivity().setPhoneNumber(number.getText().toString());
                retrieveCustomerStatus();
            }
        });
    }

    //endregion


    private final TextView.OnEditorActionListener numberEditorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_GO || (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                next();

                return true;
            }

            return false;
        }
    };
}
