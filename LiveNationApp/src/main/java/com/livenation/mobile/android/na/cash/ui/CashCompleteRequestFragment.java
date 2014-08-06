package com.livenation.mobile.android.na.cash.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
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
import com.livenation.mobile.android.na.cash.service.responses.CashMoney;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashCompleteRequestFragment extends ListFragment implements ContactDataAdapter.DataProvider {
    @InjectView(R.id.fragment_cash_complete_amount) TextView amount;

    private ContactDataAdapter adapter;
    private HashMap<String, Integer> quantities;
    private long pricePerTicket;
    private boolean hasInitiatedPayment = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new ContactDataAdapter(getActivity(), ContactDataAdapter.Mode.REVIEW, this);
        this.quantities = getCashRequestActivity().getQuantities();
        this.pricePerTicket = CashUtils.calculatePricePerTicket(getCashRequestActivity().getTotal(), getCashRequestActivity().getQuantity());

        setListAdapter(adapter);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_complete_request, container, false);
        ButterKnife.inject(this, view);

        long amountRequested = pricePerTicket * (getCashRequestActivity().getQuantity() - 1);
        BigDecimal formatableAmountRequested = BigDecimal.valueOf(amountRequested / 100.0);
        amount.setText(getString(R.string.cash_confirmation_title_fmt, TicketingUtils.formatCurrency(null, formatableAmountRequested)));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initiatePayment();
    }


    private CashCompleteRequestActivity getCashRequestActivity() {
        return (CashCompleteRequestActivity) getActivity();
    }


    private ArrayList<CashPayment> buildPayments() {
        ArrayList<CashPayment> payments = new ArrayList<CashPayment>();
        for (ContactData contact : getCashRequestActivity().getContacts()) {
            int quantity = quantities.get(contact.getId());

            CashPayment payment = new CashPayment();
            payment.setAction(CashPayment.ACTION_REQUEST);
            payment.setPaymentId(UUID.randomUUID().toString());

            CashCustomer sender = new CashCustomer();
            if (!TicketingUtils.isCollectionEmpty(contact.getPhoneNumbers())) {
                sender.setPhoneNumber(contact.getPhoneNumbers().get(0).getPhoneNumber());
            }

            if (!TicketingUtils.isCollectionEmpty(contact.getEmails())) {
                sender.setEmail(contact.getEmails().get(0));
            }
            payment.setSender(sender);

            CashMoney amount = new CashMoney();
            amount.setCurrencyCode(CashMoney.CURRENCY_CODE_USD);
            amount.setAmount(pricePerTicket * quantity);
            payment.setAmount(amount);

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
                    }

                    adapter.addAll(getCashRequestActivity().getContacts());
                }
            });
        }
    }


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
}
