package com.livenation.mobile.android.na.cash.ui.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.ticketing.adapters.CardIssuerAdapter;
import com.livenation.mobile.android.ticketing.data.CardIssuer;
import com.livenation.mobile.android.ticketing.utils.forms.ValidationManager;
import com.livenation.mobile.android.ticketing.utils.forms.listeners.EditTextValidationListener;
import com.livenation.mobile.android.ticketing.utils.forms.validators.MonthValidator;
import com.livenation.mobile.android.ticketing.utils.forms.validators.NumberValidator;
import com.livenation.mobile.android.ticketing.widgets.CreditCardEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashCardFragment extends Fragment {
    @InjectView(R.id.fragment_cash_card_issuer) Spinner cardIssuer;
    @InjectView(R.id.fragment_cash_card_number) CreditCardEditText cardNumber;
    @InjectView(R.id.fragment_cash_card_month) EditText cardExprMonth;
    @InjectView(R.id.fragment_cash_card_year) EditText cardExprYear;

    private CardIssuerAdapter cardIssuerAdapter;
    private ValidationManager validationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.validationManager = new ValidationManager();

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_card, container, false);
        ButterKnife.inject(this, view);

        setupCardInput();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tearDownCardInput();
    }


    private void setupCardInput() {
        this.cardIssuerAdapter = new CardIssuerAdapter(getActivity());
        cardIssuer.setAdapter(cardIssuerAdapter);

        cardIssuer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                CardIssuer issuer = (CardIssuer) adapterView.getSelectedItem();
                validationManager.detach(cardNumber);
                validationManager.attach(cardNumber, issuer.getValidator(), new EditTextValidationListener());
                cardNumber.setNumberLength(issuer.getNumberLength());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                validationManager.detach(cardNumber);
                cardNumber.setNumberLength(0);
            }
        });

        cardNumber.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        validationManager.attach(cardExprMonth, new MonthValidator(), new EditTextValidationListener());
        cardExprMonth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        validationManager.attach(cardExprYear, new NumberValidator(), new EditTextValidationListener());
        cardExprYear.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }

    private void tearDownCardInput() {
        validationManager.detach(cardNumber);
        validationManager.detach(cardExprMonth);
        validationManager.detach(cardExprYear);
    }
}
