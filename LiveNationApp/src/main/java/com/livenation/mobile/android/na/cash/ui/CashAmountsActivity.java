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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;
import com.livenation.mobile.android.na.cash.ui.dialogs.CashLoadingDialogFragment;
import com.livenation.mobile.android.na.cash.ui.onboarding.CashOnboardingActivity;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Total;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;

import static rx.android.observables.AndroidObservable.bindActivity;

public class CashAmountsActivity extends LiveNationFragmentActivity {
    private CashAmountsFragment fragment;

    private final BroadcastReceiver requestsCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(@NonNull Context context, @NonNull Intent intent) {
            finish();
        }
    };

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_amounts);

        this.fragment = (CashAmountsFragment) getSupportFragmentManager().findFragmentById(R.id.activity_cash_amounts_fragment);

        //noinspection ConstantConditions
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
        private int calculateTotalUsedQuantity(HashMap<String, Integer> quantities) {
            int sum = 1;
            for (Map.Entry<String, Integer> entry : quantities.entrySet())
                sum += entry.getValue();

            return sum;
        }

        private void showOnBoarding(CashCustomerStatus status, HashMap<String, Integer> quantities) {
            Intent intent = new Intent(CashAmountsActivity.this, CashOnboardingActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(CashUtils.EXTRA_TICKET_PER_CONTACT_QUANTITIES, quantities);
            intent.putExtra(CashUtils.EXTRA_CUSTOMER_STATUS, status);
            intent.putExtra(CashUtils.EXTRA_USED_TICKET_QUANTITY, calculateTotalUsedQuantity(quantities));
            startActivity(intent);
        }

        private void tryImplicitSessionOpen(final HashMap<String, Integer> quantities) {
            final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();
            loadingDialogFragment.show(getSupportFragmentManager(), CashLoadingDialogFragment.TAG);

            final SquareCashService service = SquareCashService.getInstance();
            String phoneNumber = service.getStoredPhoneNumber();
            service.startSession(null, phoneNumber, new SquareCashService.ApiCallback<CashSession>() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadingDialogFragment.dismiss();
                    showOnBoarding(null, quantities);
                }

                @Override
                public void onResponse(CashSession response) {
                    Observable<CashCustomerStatus> status = bindActivity(CashAmountsActivity.this, service.retrieveCustomerStatus());
                    status.subscribe(new Observer<CashCustomerStatus>() {
                        @Override
                        public void onCompleted() {
                            loadingDialogFragment.dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            showOnBoarding(null, quantities);
                        }

                        @Override
                        public void onNext(CashCustomerStatus customerStatus) {
                            if (customerStatus.isBlocked())
                                showOnBoarding(customerStatus, quantities);
                            else
                                showComplete(quantities);
                        }
                    });
                }
            });
        }

        private void showComplete(HashMap<String, Integer> quantities) {
            Intent intent = new Intent(CashAmountsActivity.this, CashCompleteRequestActivity.class);
            intent.putExtras(getIntent().getExtras());
            intent.putExtra(CashUtils.EXTRA_TICKET_PER_CONTACT_QUANTITIES, quantities);
            intent.putExtra(CashUtils.EXTRA_USED_TICKET_QUANTITY, calculateTotalUsedQuantity(quantities));
            startActivity(intent);
        }

        @Override
        public void onClick(@NonNull View view) {
            HashMap<String, Integer> quantities = fragment.getTicketPerContactQuantities();
            if (quantities.isEmpty()) {
                NoQuantitySelectedDialogFragment dialogFragment = new NoQuantitySelectedDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), NoQuantitySelectedDialogFragment.TAG);
                return;
            }

            CashCustomerStatus status = getCustomerStatus();
            if (SquareCashService.getInstance().hasSession() && status != null && !status.isBlocked()) {
                showComplete(quantities);
            } else if (!TextUtils.isEmpty(SquareCashService.getInstance().getStoredPhoneNumber())) {
                tryImplicitSessionOpen(quantities);
            } else {
                showOnBoarding(null, quantities);
            }
        }
    }


    public static class NoQuantitySelectedDialogFragment extends DialogFragment {
        public static final String TAG = NoQuantitySelectedDialogFragment.class.getSimpleName();

        @Override
        public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.error_title_generic);
            builder.setMessage(R.string.square_cash_error_zero_quantities);
            builder.setPositiveButton(android.R.string.ok, null);

            return builder.create();
        }
    }
}
