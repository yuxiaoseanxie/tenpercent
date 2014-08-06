package com.livenation.mobile.android.na.cash.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactDataAdapter;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashQuantityDialogFragment;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CashAmountsFragment extends ListFragment implements ContactDataAdapter.DataProvider {
    private static final int SELECT_QUANTITY_REQUEST_CODE = 0xf;

    private ContactDataAdapter adapter;
    private final HashMap<String, Integer> quantities = new HashMap<String, Integer>();
    private int remainingQuantity = 0;
    private BigDecimal pricePerTicket = BigDecimal.ZERO;

    private int selectQuantityPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @SuppressWarnings("unchecked")
        HashSet<ContactData> contacts = getCashAmountsActivity().getContacts();
        this.adapter = new ContactDataAdapter(getActivity(), ContactDataAdapter.Mode.REVIEW, this);
        adapter.addAll(contacts);

        calculatePricePerTicket();
        calculateQuantities();

        setListAdapter(adapter);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_QUANTITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            int newQuantity = data.getIntExtra(CashQuantityDialogFragment.ARG_VALUE, 1);
            ContactData contact = adapter.getItem(selectQuantityPosition);
            quantities.put(contact.getId(), newQuantity);

            recalculateRemainingQuantity();
            adapter.notifyDataSetChanged();

            getActivity().invalidateOptionsMenu();
        }
    }


    //region Calculations

    private void calculatePricePerTicket() {
        BigDecimal total = getCashAmountsActivity().getTotal().getGrandTotal();
        BigDecimal quantity = BigDecimal.valueOf(getCashAmountsActivity().getQuantity());
        this.pricePerTicket = total.divide(quantity, RoundingMode.HALF_EVEN);
    }

    private void calculateQuantities() {
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            ContactData contact = adapter.getItem(i);
            quantities.put(contact.getId(), 1);
        }

        int totalQuantity = getCashAmountsActivity().getQuantity();
        int usedQuantity = adapter.getCount();
        remainingQuantity = (totalQuantity - usedQuantity) - 1;
    }

    private void recalculateRemainingQuantity() {
        int totalQuantity = getCashAmountsActivity().getQuantity() - 1;
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            totalQuantity -= entry.getValue();
        }

        remainingQuantity = totalQuantity;
    }

    //endregion


    //region Properties

    private CashAmountsActivity getCashAmountsActivity() {
        return (CashAmountsActivity) getActivity();
    }


    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public HashMap<String, Integer> getQuantities() {
        return quantities;
    }

    //endregion


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
        BigDecimal price = pricePerTicket.multiply(BigDecimal.valueOf(quantity));
        return TicketingUtils.formatCurrency(null, price);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        this.selectQuantityPosition = position;

        ContactData contact = adapter.getItem(position);
        int currentQuantity = quantities.get(contact.getId());
        int maxQuantity = currentQuantity + remainingQuantity;

        CashQuantityDialogFragment dialogFragment = CashQuantityDialogFragment.newInstance(maxQuantity, currentQuantity);
        dialogFragment.setTargetFragment(this, SELECT_QUANTITY_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), CashQuantityDialogFragment.TAG);
    }
}
