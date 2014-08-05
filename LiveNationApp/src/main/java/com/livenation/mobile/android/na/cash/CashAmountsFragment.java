package com.livenation.mobile.android.na.cash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ListView;

import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactDataAdapter;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;

public class CashAmountsFragment extends ListFragment implements ContactDataAdapter.PriceProvider {
    private static final int SELECT_QUANTITY_REQUEST_CODE = 0xf;

    private ContactDataAdapter adapter;
    private final SparseIntArray quantities = new SparseIntArray();
    private int remainingQuantity = 0;
    private BigDecimal pricePerTicket = BigDecimal.ZERO;

    private int selectQuantityPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @SuppressWarnings("unchecked")
        HashSet<ContactData> contacts = getCashAmountsActivity().getContacts();
        this.adapter = new ContactDataAdapter(getActivity(), null);
        adapter.setPriceProvider(this);
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
            quantities.put(selectQuantityPosition, newQuantity);

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
            quantities.put(i, 1);
        }

        int totalQuantity = getCashAmountsActivity().getQuantity();
        int usedQuantity = adapter.getCount();
        remainingQuantity = (totalQuantity - usedQuantity) - 1;
    }

    private void recalculateRemainingQuantity() {
        int totalQuantity = getCashAmountsActivity().getQuantity() - 1;
        for (int i = 0, count = quantities.size(); i < count; i++) {
            int quantity = quantities.get(i);
            totalQuantity -= quantity;
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

    //endregion


    @Override
    public @NonNull String getPrice(int position, @NonNull ContactData contact) {
        int quantity = quantities.get(position);
        BigDecimal price = pricePerTicket.multiply(BigDecimal.valueOf(quantity));
        return TicketingUtils.formatCurrency(null, price);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        this.selectQuantityPosition = position;

        int currentQuantity = quantities.get(position);
        int maxQuantity = currentQuantity + remainingQuantity;

        CashQuantityDialogFragment dialogFragment = CashQuantityDialogFragment.newInstance(maxQuantity, currentQuantity);
        dialogFragment.setTargetFragment(this, SELECT_QUANTITY_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), CashQuantityDialogFragment.TAG);
    }
}
