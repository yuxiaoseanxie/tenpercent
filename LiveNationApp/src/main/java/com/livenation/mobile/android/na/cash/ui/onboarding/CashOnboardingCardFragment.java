package com.livenation.mobile.android.na.cash.ui.onboarding;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.util.Log;
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
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkInfo;
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkResponse;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.ticketing.utils.forms.ValidationManager;
import com.livenation.mobile.android.ticketing.utils.forms.listeners.EditTextValidationListener;
import com.livenation.mobile.android.ticketing.utils.forms.validators.CardChecksumValidator;
import com.livenation.mobile.android.ticketing.utils.forms.validators.MonthValidator;
import com.livenation.mobile.android.ticketing.utils.forms.validators.NumberValidator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnEditorAction;

public class CashOnboardingCardFragment extends CashOnboardingFragment {
    @InjectView(R.id.fragment_cash_onboarding_card_number) EditText cardNumber;
    @InjectView(R.id.fragment_cash_onboarding_card_month) EditText cardExprMonth;
    @InjectView(R.id.fragment_cash_onboarding_card_year) EditText cardExprYear;
    @InjectView(R.id.fragment_cash_onboarding_card_cvv) EditText cardCvv;
    @InjectView(R.id.fragment_cash_onboarding_card_postal) EditText cardPostal;

    private ValidationManager validationManager;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.validationManager = new ValidationManager();

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_onboarding_card, container, false);
        ButterKnife.inject(this, view);

        setupCardInput();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tearDownCardInput();
    }

    //endregion


    //region Validation

    private void setupCardInput() {
        validationManager.attach(cardNumber, new CardChecksumValidator(), new EditTextValidationListener());
        cardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});

        validationManager.attach(cardExprMonth, new MonthValidator(), new EditTextValidationListener());
        cardExprMonth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        validationManager.attach(cardExprYear, new NumberValidator(), new EditTextValidationListener());
        cardExprYear.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        validationManager.attach(cardCvv, new NumberValidator(), new EditTextValidationListener());
        cardCvv.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        // If/when we add support for Canada, we will need to remove these constraints.
        validationManager.attach(cardPostal, new NumberValidator(), new EditTextValidationListener());
        cardPostal.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
    }

    private void tearDownCardInput() {
        validationManager.detach(cardNumber);
        validationManager.detach(cardExprMonth);
        validationManager.detach(cardExprYear);
        validationManager.detach(cardCvv);
        validationManager.detach(cardPostal);
    }

    //endregion


    //region Linking Cards

    @Override
    public void next() {
        CashUtils.dismissKeyboard(getActivity().getCurrentFocus());

        CashCardLinkInfo linkInfo = new CashCardLinkInfo();
        linkInfo.setNumber(cardNumber.getText().toString());
        linkInfo.setExpiration(cardExprMonth.getText().toString(), cardExprYear.getText().toString());
        linkInfo.setSecurityCode(cardCvv.getText().toString());
        linkInfo.setPostalCode(cardPostal.getText().toString());

        final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);
        SquareCashService.getInstance().linkCard(linkInfo, new SquareCashService.ApiCallback<CashCardLinkResponse>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialogFragment.dismiss();
                CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
            }

            @Override
            public void onResponse(CashCardLinkResponse response) {
                Log.i(CashUtils.LOG_TAG, "Got response " + response);
                loadingDialogFragment.dismiss();

                if (response.isValid()) {
                    getCashRequestDetailsActivity().continueToName();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.error_title_generic);
                    builder.setMessage(response.getErrorMessage());
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.show();
                }
            }
        });
    }


    @OnEditorAction(R.id.fragment_cash_onboarding_card_postal)
    public boolean onPostalEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            next();

            return true;
        }
        return false;
    }

    //endregion
}
