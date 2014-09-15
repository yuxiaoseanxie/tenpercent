package com.livenation.mobile.android.na.cash.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashIntroductionDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Total;

public class CashRecipientsActivity extends LiveNationFragmentActivity {
    private MenuItem nextItem;
    private CashRecipientsFragment fragment;

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
        setContentView(R.layout.activity_cash_recipients);

        this.fragment = (CashRecipientsFragment) getSupportFragmentManager().findFragmentById(R.id.activity_cash_recipients_fragment);

        //noinspection ConstantConditions
        getActionBar().setSubtitle(getResources().getQuantityString(R.plurals.cash_transaction_detail, getTicketQuantity(), getTicketQuantity(), TicketingUtils.formatCurrency(null, getTotal().getGrandTotal())));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(requestsCompletedReceiver, new IntentFilter(CashUtils.ACTION_REQUESTS_COMPLETED));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (CashIntroductionDialogFragment.shouldShow() &&
            getSupportFragmentManager().findFragmentByTag(CashIntroductionDialogFragment.TAG) == null) {
            CashIntroductionDialogFragment dialogFragment = CashIntroductionDialogFragment.newInstance(getTotal(), getTicketQuantity(), getEvent());
            dialogFragment.show(getSupportFragmentManager(), CashIntroductionDialogFragment.TAG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(requestsCompletedReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cash, menu);

        this.nextItem = menu.findItem(R.id.action_next);
        nextItem.getActionView().setOnClickListener(new NextClickListener());

        return super.onCreateOptionsMenu(menu);
    }

    //endregion


    public Event getEvent() {
        return (Event) getIntent().getSerializableExtra(CashUtils.EXTRA_EVENT);
    }

    public int getTicketQuantity() {
        return getIntent().getIntExtra(CashUtils.EXTRA_TICKET_QUANTITY, 0);
    }

    public Total getTotal() {
        return (Total) getIntent().getSerializableExtra(CashUtils.EXTRA_TOTAL);
    }


    private class NextClickListener implements View.OnClickListener {
        private void showAmountsActivity(CashCustomerStatus customerStatus) {
            Intent intent = new Intent(CashRecipientsActivity.this, CashAmountsActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(CashUtils.EXTRA_NOTE, fragment.getNote());
            intent.putExtra(CashUtils.EXTRA_CONTACTS, fragment.getSelectedContacts());
            intent.putExtra(CashUtils.EXTRA_CUSTOMER_STATUS, customerStatus);
            startActivity(intent);
        }

        private void requestCustomerStatus() {
            final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
            loadingDialogFragment.show(getSupportFragmentManager(), CashLoadingDialogFragment.TAG);

            SquareCashService.getInstance().retrieveCustomerStatus(new SquareCashService.ApiCallback<CashCustomerStatus>() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Check for session token invalid error here.
                    loadingDialogFragment.dismiss();
                    CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                    errorDialogFragment.show(getSupportFragmentManager(), CashErrorDialogFragment.TAG);
                }

                @Override
                public void onResponse(CashCustomerStatus response) {
                    loadingDialogFragment.dismiss();
                    showAmountsActivity(response);
                }
            });
        }

        private void showNoContactsMessage() {
            DialogFragment noContactsDialogFragment = new DialogFragment() {
                @Override
                public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CashRecipientsActivity.this);
                    builder.setTitle(R.string.error_title_generic);
                    builder.setMessage(R.string.cash_no_contacts_error_message);
                    builder.setPositiveButton(android.R.string.ok, null);
                    return builder.create();
                }
            };
            noContactsDialogFragment.show(getSupportFragmentManager(), "NoContactsDialogFragment");
        }

        @Override
        public void onClick(View view) {
            if (!fragment.hasContactsSelected()) {
                fragment.forceCompletion();
                if (!fragment.hasContactsSelected()) {
                    showNoContactsMessage();
                    return;
                }
            }

            if (SquareCashService.getInstance().hasSession())
                requestCustomerStatus();
            else
                showAmountsActivity(null);
        }
    }
}
