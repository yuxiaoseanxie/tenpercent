package com.livenation.mobile.android.na.cash.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactDataAdapter;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashQuantityDialogFragment;
import com.livenation.mobile.android.na.cash.ui.views.CharacterDrawable;
import com.livenation.mobile.android.na.cash.ui.views.ContactView;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashAmountsFragment extends ListFragment implements ContactDataAdapter.DataProvider {
    private static final int SELECT_QUANTITY_REQUEST_CODE = 0xf;

    @InjectView(R.id.fragment_cash_amounts_artist_text) TextView artistText;
    @InjectView(R.id.fragment_cash_amounts_date_text) TextView dateText;
    @InjectView(R.id.fragment_cash_amounts_price_per_ticket_text) TextView pricePerTicketText;
    @InjectView(R.id.cash_request_total_text) TextView totalText;

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

        setListAdapter(adapter);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_amounts, container, false);
        ButterKnife.inject(this, view);

        artistText.setText(getCashAmountsActivity().getEvent().getDisplayName());
        dateText.setText(TicketingUtils.formatDate(getCashAmountsActivity().getEvent().getLocalStartTime()));

        calculatePricePerTicket();
        calculateQuantities();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.headerView = new ContactView(getActivity());
        headerView.getPhotoImageView().setImageDrawable(new CharacterDrawable('Y', 0xFFeaeaea));
        headerView.setName(getString(R.string.cash_contact_name_you));
        headerView.setPrice(TicketingUtils.formatCurrency(null, pricePerTicket));
        headerView.setQuantity(1);
        headerView.setEditable(false);
        getListView().addHeaderView(headerView, null, false);

        this.footerView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, getListView(), false);
        TextView footerText = (TextView) footerView;
        footerText.setText(getString(R.string.cash_logout_text));
        footerText.setGravity(Gravity.CENTER);
        footerText.setBackgroundResource(android.R.drawable.list_selector_background);
        footerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
        if (SquareCashService.getInstance().hasSession())
            getListView().addFooterView(footerText, null, false);
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

    public HashMap<String, Integer> getTicketPerContactQuantities() {
        return quantities;
    }

    //endregion


    //region List View


    @NonNull
    @Override
    public String getPrice(int position, @NonNull ContactData contact) {
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

        CashQuantityDialogFragment dialogFragment = CashQuantityDialogFragment.newInstance(maxQuantity, currentQuantity);
        dialogFragment.setTargetFragment(this, SELECT_QUANTITY_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), CashQuantityDialogFragment.TAG);
    }

    //endregion


    private void logout() {
        SquareCashService.getInstance().clearSession();
        getListView().removeFooterView(footerView);
    }
}
