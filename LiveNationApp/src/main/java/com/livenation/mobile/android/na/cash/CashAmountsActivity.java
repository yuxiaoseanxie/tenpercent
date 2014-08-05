package com.livenation.mobile.android.na.cash;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Total;

import java.util.HashSet;

public class CashAmountsActivity extends LiveNationFragmentActivity {
    public static final String EXTRA_CONTACTS = "com.livenation.mobile.android.na.cash.CashAmountsActivity.EXTRA_CONTACTS";

    private CashAmountsFragment fragment;
    private MenuItem nextItem;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_amounts);

        this.fragment = (CashAmountsFragment) getSupportFragmentManager().findFragmentById(R.id.activity_cash_amounts_fragment);

        getActionBar().setSubtitle(getResources().getQuantityString(R.plurals.cash_transaction_detail, getQuantity(), getQuantity(), TicketingUtils.formatCurrency(null, getTotal().getGrandTotal())));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cash, menu);

        this.nextItem = menu.findItem(R.id.action_next);
        nextItem.getActionView().setOnClickListener(new NextClickListener());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        nextItem.getActionView().setEnabled(fragment.getRemainingQuantity() == 0);

        return super.onPrepareOptionsMenu(menu);
    }

    //endregion


    public int getQuantity() {
        return getIntent().getIntExtra(CashRecipientsActivity.EXTRA_QUANTITY, 0);
    }

    public Total getTotal() {
        return (Total) getIntent().getSerializableExtra(CashRecipientsActivity.EXTRA_TOTAL);
    }

    @SuppressWarnings("unchecked")
    public HashSet<ContactData> getContacts() {
        return (HashSet<ContactData>) getIntent().getSerializableExtra(CashAmountsActivity.EXTRA_CONTACTS);
    }


    private class NextClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CashAmountsActivity.this, CashRequestDetailsActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(CashRequestDetailsActivity.EXTRA_QUANTITIES, fragment.getQuantities());
            startActivity(intent);
        }
    }
}
