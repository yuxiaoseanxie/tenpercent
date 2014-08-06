package com.livenation.mobile.android.na.cash.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.mobilitus.tm.tickets.models.Total;

import java.util.HashMap;
import java.util.HashSet;

public class CashCompleteRequestActivity extends LiveNationFragmentActivity {
    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_complete_request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cash, menu);

        TextView nextView = (TextView) menu.findItem(R.id.action_next).getActionView();
        nextView.setText(R.string.cash_action_done);
        nextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //endregion


    //region Getters

    public int getTicketQuantity() {
        return getIntent().getIntExtra(CashUtils.EXTRA_TICKET_QUANTITY, 0);
    }

    public Total getTotal() {
        return (Total) getIntent().getSerializableExtra(CashUtils.EXTRA_TOTAL);
    }

    @SuppressWarnings("unchecked")
    public HashSet<ContactData> getContacts() {
        return (HashSet<ContactData>) getIntent().getSerializableExtra(CashUtils.EXTRA_CONTACTS);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Integer> getTicketPerContactQuantities() {
        return (HashMap<String, Integer>) getIntent().getSerializableExtra(CashUtils.EXTRA_TICKET_PER_CONTACT_QUANTITIES);
    }

    //endregion
}
