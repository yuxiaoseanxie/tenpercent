package com.livenation.mobile.android.na.cash.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactDataAdapter;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomer;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomization;
import com.livenation.mobile.android.na.cash.service.responses.CashMoney;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashCompleteRequestFragment extends ListFragment implements ContactDataAdapter.DataProvider {
    @InjectView(R.id.fragment_cash_complete_amount) TextView amount;

    private ContactDataAdapter adapter;
    private HashMap<String, Integer> quantities;
    private long pricePerTicket;
    private boolean hasInitiatedPayment = false;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new ContactDataAdapter(getActivity(), ContactDataAdapter.Mode.REVIEW, this);
        this.quantities = getCashRequestActivity().getTicketPerContactQuantities();
        this.pricePerTicket = CashUtils.calculatePricePerTicket(getCashRequestActivity().getTotal(), getCashRequestActivity().getTicketQuantity());

        setListAdapter(adapter);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_complete_request, container, false);
        ButterKnife.inject(this, view);

        long amountRequested = pricePerTicket * (getCashRequestActivity().getTicketQuantity() - 1);
        BigDecimal formatableAmountRequested = BigDecimal.valueOf(amountRequested / 100.0);
        amount.setText(getString(R.string.cash_confirmation_title_fmt, TicketingUtils.formatCurrency(null, formatableAmountRequested)));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initiatePayment();
    }

    //endregion


    private CashCompleteRequestActivity getCashRequestActivity() {
        return (CashCompleteRequestActivity) getActivity();
    }


    //region Sending Payments

    private ArrayList<CashPayment> buildPayments() {
        CashCustomization senderCustomization = new CashCustomization(getCashRequestActivity().getNote());
        ArrayList<CashPayment> payments = new ArrayList<CashPayment>();
        for (ContactData contact : getCashRequestActivity().getContacts()) {
            int quantity = quantities.get(contact.getId());

            CashPayment payment = CashPayment.newRequest();
            payment.setSenderCustomization(senderCustomization);
            payment.setAmount(CashMoney.newUSD(pricePerTicket * quantity));
            payment.setSender(CashCustomer.fromContactData(contact));

            payments.add(payment);
        }

        return payments;
    }

    private void initiatePayment() {
        if (hasInitiatedPayment)
            return;

        hasInitiatedPayment = true;

        final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        ArrayList<CashPayment> payments = buildPayments();
        final AtomicInteger counter = new AtomicInteger(payments.size());
        for (CashPayment payment : payments) {
            SquareCashService.getInstance().initiatePayment(payment, new SquareCashService.ApiCallback<CashPayment>() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (counter.decrementAndGet() == 0) {
                        loadingDialogFragment.dismiss();
                    }

                    CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                    errorDialogFragment.show(getFragmentManager(), CashErrorDialogFragment.TAG);
                }

                @Override
                public void onResponse(CashPayment response) {
                    if (counter.decrementAndGet() == 0) {
                        loadingDialogFragment.dismiss();

                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(CashUtils.ACTION_REQUESTS_COMPLETED));
                    }

                    adapter.addAll(getCashRequestActivity().getContacts());
                }
            });
        }
    }

    //endregion


    //region Contacts Adapter

    @Override
    public @NonNull String getBigDetails(int position, @NonNull ContactData contact) {
        int quantity = quantities.get(contact.getId());
        return Integer.toString(quantity);
    }

    @Override
    public boolean isContactSelected(int position, @NonNull ContactData contact) {
        return false;
    }

    @Override
    public @NonNull String getSmallDetails(int position, @NonNull ContactData contact) {
        int quantity = quantities.get(contact.getId());
        BigDecimal price = BigDecimal.valueOf((pricePerTicket * quantity) / 100.0);
        return TicketingUtils.formatCurrency(null, price);
    }

    //endregion
}
