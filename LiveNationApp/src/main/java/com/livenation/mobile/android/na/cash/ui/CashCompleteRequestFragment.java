package com.livenation.mobile.android.na.cash.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomer;
import com.livenation.mobile.android.na.cash.service.responses.CashMoney;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.ButterKnife;

public class CashCompleteRequestFragment extends Fragment {
    private boolean hasInitiatedPayment = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_request, container, false);
        ButterKnife.inject(this, view);
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


    private long getPricePerTicket() {
        BigDecimal total = getCashRequestActivity().getTotal().getGrandTotal();
        BigDecimal quantity = BigDecimal.valueOf(getCashRequestActivity().getQuantity());
        BigDecimal pricePerTicket = total.divide(quantity, RoundingMode.HALF_EVEN);
        return pricePerTicket.longValue() * 100;
    }

    private ArrayList<CashPayment> buildPayments() {
        CashMoney amount = new CashMoney();
        amount.setCurrencyCode(CashMoney.CURRENCY_CODE_USD);
        amount.setAmount(getPricePerTicket());

        ArrayList<CashPayment> payments = new ArrayList<CashPayment>();
        for (ContactData contact : getCashRequestActivity().getContacts()) {
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
                }
            });
        }
    }
}
