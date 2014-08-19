package com.livenation.mobile.android.na.cash.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomer;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.na.cash.ui.onboarding.CashOnboardingActivity;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Total;

import java.util.ArrayList;

public class CashAmountsActivity extends LiveNationFragmentActivity {
    private CashAmountsFragment fragment;

    private final BroadcastReceiver requestsCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_amounts);

        this.fragment = (CashAmountsFragment) getSupportFragmentManager().findFragmentById(R.id.activity_cash_amounts_fragment);

        getActionBar().setSubtitle(getResources().getQuantityString(R.plurals.cash_transaction_detail, getTicketQuantity(), getTicketQuantity(), TicketingUtils.formatCurrency(null, getTotal().getGrandTotal())));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(requestsCompletedReceiver, new IntentFilter(CashUtils.ACTION_REQUESTS_COMPLETED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(requestsCompletedReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cash, menu);

        MenuItem nextItem = menu.findItem(R.id.action_next);
        TextView actionView = (TextView) nextItem.getActionView();
        actionView.setText(R.string.cash_action_request);
        actionView.setOnClickListener(new NextClickListener());

        return super.onCreateOptionsMenu(menu);
    }

    //endregion


    //region Properties

    public Event getEvent() {
        return (Event) getIntent().getSerializableExtra(CashUtils.EXTRA_EVENT);
    }

    public int getTicketQuantity() {
        return getIntent().getIntExtra(CashUtils.EXTRA_TICKET_QUANTITY, 0);
    }

    public Total getTotal() {
        return (Total) getIntent().getSerializableExtra(CashUtils.EXTRA_TOTAL);
    }

    public CashCustomerStatus getCustomerStatus() {
        return (CashCustomerStatus) getIntent().getSerializableExtra(CashUtils.EXTRA_CUSTOMER_STATUS);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ContactData> getContacts() {
        return (ArrayList<ContactData>) getIntent().getSerializableExtra(CashUtils.EXTRA_CONTACTS);
    }

    //endregion


    private class NextClickListener implements View.OnClickListener {
        private void showOnBoarding(CashCustomerStatus status) {
            Intent intent = new Intent(CashAmountsActivity.this, CashOnboardingActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(CashUtils.EXTRA_TICKET_PER_CONTACT_QUANTITIES, fragment.getTicketPerContactQuantities());
            intent.putExtra(CashUtils.EXTRA_CUSTOMER_STATUS, status);
            startActivity(intent);
        }

        private void showComplete() {
            Intent intent = new Intent(CashAmountsActivity.this, CashCompleteRequestActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(CashUtils.EXTRA_TICKET_PER_CONTACT_QUANTITIES, fragment.getTicketPerContactQuantities());
            startActivity(intent);
        }

        @Override
        public void onClick(View view) {
            CashCustomerStatus status = getCustomerStatus();
            if (SquareCashService.getInstance().hasSession() && status != null && !status.isBlocked()) {
                showComplete();
            } else {
                showOnBoarding(null);
            }
        }
    }
}
