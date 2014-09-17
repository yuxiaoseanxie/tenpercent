package com.livenation.mobile.android.na.cash.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactDataAdapter;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashQuantityDialogFragment;
import com.livenation.mobile.android.na.cash.ui.views.ContactView;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CashAmountsFragment extends ListFragment implements ContactDataAdapter.DataProvider {
    private static final int SELECT_QUANTITY_REQUEST_CODE = 0xf;

    private TextView artistText;
    private TextView dateText;
    private TextView pricePerTicketText;
    private TextView totalText;

    private ContactDataAdapter adapter;
    private ContactView headerView;
    private View footerView;

    private final HashMap<String, Integer> quantities = new HashMap<String, Integer>();
    private int remainingQuantity = 0;
    private BigDecimal pricePerTicket = BigDecimal.ZERO;

    private int selectQuantityPosition = 0;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        @SuppressWarnings("unchecked")
        ArrayList<ContactData> contacts = getCashAmountsActivity().getContacts();
        this.adapter = new ContactDataAdapter(getActivity(), this, contacts);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_amounts, container, false);

        this.artistText = (TextView) view.findViewById(R.id.fragment_cash_amounts_artist_text);
        this.dateText = (TextView) view.findViewById(R.id.fragment_cash_amounts_date_text);
        this.pricePerTicketText = (TextView) view.findViewById(R.id.fragment_cash_amounts_price_per_ticket_text);
        this.totalText = (TextView) view.findViewById(R.id.cash_request_total_text);

        artistText.setText(getCashAmountsActivity().getEvent().getDisplayName());
        dateText.setText(TicketingUtils.formatDate(getCashAmountsActivity().getEvent().getLocalStartTime()));

        calculatePricePerTicket();
        calculateQuantities();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String fullName = getFullName();

        this.headerView = new ContactView(getActivity());
        headerView.getPhotoImageView().setImageResource(R.drawable.ic_contact_picture);
        headerView.setName(fullName);
        headerView.setPrice(TicketingUtils.formatCurrency(null, pricePerTicket));
        headerView.setQuantity(1);
        headerView.setEditable(false);
        getListView().addHeaderView(headerView, null, false);

        this.footerView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_cash_logout_item, getListView(), false);
        TextView footerText = (TextView) footerView;
        footerText.setText(getString(R.string.cash_logout_text_fmt, fullName));
        footerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                logout();
            }
        });
        if (SquareCashService.getInstance().hasSession())
            getListView().addFooterView(footerText, null, false);

        setListAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_QUANTITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            int newQuantity = data.getIntExtra(CashQuantityDialogFragment.ARG_VALUE, 1);
            ContactData contact = (ContactData) getListView().getItemAtPosition(selectQuantityPosition);
            quantities.put(contact.getId(), newQuantity);

            recalculateRemainingQuantity();
            adapter.notifyDataSetChanged();
        }
    }

    //endregion


    //region Calculations

    private void calculatePricePerTicket() {
        BigDecimal total = getCashAmountsActivity().getTotal().getGrandTotal();
        BigDecimal quantity = BigDecimal.valueOf(getCashAmountsActivity().getTicketQuantity());
        this.pricePerTicket = total.divide(quantity, RoundingMode.HALF_EVEN);
        pricePerTicketText.setText(getString(R.string.cash_request_price_per_ticket_fmt, TicketingUtils.formatCurrency(null, pricePerTicket)));
    }

    private void calculateQuantities() {
        for (int i = 0, count = adapter.getCount(); i < count; i++) {
            ContactData contact = adapter.getItem(i);
            quantities.put(contact.getId(), 1);
        }

        int totalQuantity = getCashAmountsActivity().getTicketQuantity();
        int usedQuantity = adapter.getCount();
        remainingQuantity = (totalQuantity - usedQuantity) - 1;

        BigDecimal totalAmount = pricePerTicket.multiply(BigDecimal.valueOf(usedQuantity));
        String amountString = TicketingUtils.formatCurrency(null, totalAmount);
        totalText.setText(amountString);
    }

    private void recalculateRemainingQuantity() {
        int usedQuantity = 0;
        remainingQuantity = getCashAmountsActivity().getTicketQuantity() - 1;
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            remainingQuantity -= entry.getValue();
            usedQuantity += entry.getValue();
        }

        BigDecimal totalAmount = pricePerTicket.multiply(BigDecimal.valueOf(usedQuantity));
        String amountString = TicketingUtils.formatCurrency(null, totalAmount);
        totalText.setText(amountString);
    }

    //endregion


    //region Properties

    private CashAmountsActivity getCashAmountsActivity() {
        return (CashAmountsActivity) getActivity();
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public String getFullName() {
        CashCustomerStatus status = getCashAmountsActivity().getCustomerStatus();
        if (status != null && !TextUtils.isEmpty(status.getFullName()))
            return status.getFullName();
        else
            return getString(R.string.cash_contact_you_placeholder);
    }

    public HashMap<String, Integer> getTicketPerContactQuantities() {
        HashMap<String, Integer> filteredQuantities = new HashMap<>(quantities);
        Iterator<Map.Entry<String, Integer>> entries = filteredQuantities.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Integer> entry = entries.next();
            if (entry.getValue() == 0)
                entries.remove();
        }
        return filteredQuantities;
    }

    //endregion


    //region List View

    @Override
    public @NonNull String getPrice(int position, @NonNull ContactData contact) {
        int quantity = quantities.get(contact.getId());
        BigDecimal price = pricePerTicket.multiply(BigDecimal.valueOf(quantity));
        return TicketingUtils.formatCurrency(null, price);
    }

    @Override
    public int getQuantity(int position, @NonNull ContactData contact) {
        return quantities.get(contact.getId());
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        this.selectQuantityPosition = position;

        ContactData contact = (ContactData) l.getItemAtPosition(position);
        int currentQuantity = quantities.get(contact.getId());
        int maxQuantity = currentQuantity + getRemainingQuantity();

        CashQuantityDialogFragment dialogFragment = CashQuantityDialogFragment.newInstance(maxQuantity, currentQuantity, pricePerTicket);
        dialogFragment.setTargetFragment(this, SELECT_QUANTITY_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), CashQuantityDialogFragment.TAG);
    }

    //endregion


    private void logout() {
        SquareCashService.getInstance().clearSession();
        getListView().removeFooterView(footerView);
    }
}
