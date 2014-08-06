package com.livenation.mobile.android.na.cash.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.ui.onboarding.CashOnBoardingActivity;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashErrorDialogFragment;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Total;

import java.util.HashSet;

public class CashAmountsActivity extends LiveNationFragmentActivity {
    private CashAmountsFragment fragment;
    private MenuItem nextItem;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_amounts);

        this.fragment = (CashAmountsFragment) getSupportFragmentManager().findFragmentById(R.id.activity_cash_amounts_fragment);

        getActionBar().setSubtitle(getResources().getQuantityString(R.plurals.cash_transaction_detail, getTicketQuantity(), getTicketQuantity(), TicketingUtils.formatCurrency(null, getTotal().getGrandTotal())));
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


    private class NextClickListener implements View.OnClickListener {
        private void showOnBoarding(CashCustomerStatus status) {
            Intent intent = new Intent(CashAmountsActivity.this, CashOnBoardingActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(CashUtils.EXTRA_TICKET_PER_CONTACT_QUANTITIES, fragment.getTicketPerContactQuantities());
            intent.putExtra(CashUtils.EXTRA_CUSTOMER_STATUS, status);
            startActivity(intent);
        }

        private void requestCustomerStatus() {
            final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
            loadingDialogFragment.show(getSupportFragmentManager(), CashLoadingDialogFragment.TAG);

            SquareCashService.getInstance().retrieveCustomerStatus(new SquareCashService.ApiCallback<CashCustomerStatus>() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadingDialogFragment.dismiss();
                    CashErrorDialogFragment errorDialogFragment = CashErrorDialogFragment.newInstance(error);
                    errorDialogFragment.show(getSupportFragmentManager(), CashErrorDialogFragment.TAG);
                }

                @Override
                public void onResponse(CashCustomerStatus response) {
                    loadingDialogFragment.dismiss();
                    if (response.isBlocked()) {
                        showOnBoarding(response);
                    } else {
                        Intent intent = new Intent(CashAmountsActivity.this, CashCompleteRequestActivity.class);
                        intent.putExtras(getIntent().getExtras());
                        intent.putExtra(CashUtils.EXTRA_TICKET_PER_CONTACT_QUANTITIES, fragment.getTicketPerContactQuantities());
                        startActivity(intent);
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (SquareCashService.getInstance().hasSession()) {
                requestCustomerStatus();
            } else {
                showOnBoarding(null);
            }
        }
    }
}
