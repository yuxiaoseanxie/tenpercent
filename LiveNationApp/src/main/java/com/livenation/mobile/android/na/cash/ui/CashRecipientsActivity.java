package com.livenation.mobile.android.na.cash.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Total;

public class CashRecipientsActivity extends LiveNationFragmentActivity {
    public static final String EXTRA_TOTAL = "com.livenation.mobile.android.na.cash.CashRecipientsActivity.EXTRA_TOTAL";
    public static final String EXTRA_QUANTITY = "com.livenation.mobile.android.na.cash.CashRecipientsActivity.EXTRA_QUANTITY";

    private MenuItem nextItem;
    private CashRecipientsFragment fragment;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_recipients);

        this.fragment = (CashRecipientsFragment) getSupportFragmentManager().findFragmentById(R.id.activity_cash_recipients_fragment);

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
        nextItem.getActionView().setEnabled(fragment.hasContactsSelected());

        return super.onPrepareOptionsMenu(menu);
    }

    //endregion


    public int getQuantity() {
        return getIntent().getIntExtra(CashRecipientsActivity.EXTRA_QUANTITY, 0);
    }

    public Total getTotal() {
        return (Total) getIntent().getSerializableExtra(CashRecipientsActivity.EXTRA_TOTAL);
    }


    private class NextClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CashRecipientsActivity.this, CashAmountsActivity.class);
            intent.putExtras(getIntent().getExtras());

            intent.putExtra(CashAmountsActivity.EXTRA_CONTACTS, fragment.getSelectedContacts());
            startActivity(intent);
        }
    }
}
