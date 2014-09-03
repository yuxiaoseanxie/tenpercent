package com.livenation.mobile.android.na.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.livenation.mobile.android.ticketing.activities.AccountActivity;
import com.livenation.mobile.android.ticketing.activities.BaseActivity;
import com.livenation.mobile.android.ticketing.analytics.TimedEvent;
import com.livenation.mobile.android.ticketing.dialogs.LoadingDialogFragment;
import com.livenation.mobile.android.na.ui.fragments.OrderHistoryFragment;
import com.livenation.mobile.android.ticketing.utils.orders.OrdersCacheManager;
import com.mobilitus.tm.tickets.TicketLibrary;
import com.mobilitus.tm.tickets.interfaces.BaseResponseListener;
import com.mobilitus.tm.tickets.interfaces.ResponseListener;
import com.mobilitus.tm.tickets.interfaces.TicketResponse;
import com.mobilitus.tm.tickets.models.Captcha;
import com.mobilitus.tm.tickets.models.Error;
import com.mobilitus.tm.tickets.models.Polling;
import com.mobilitus.tm.tickets.models.User;

public class OrderHistoryActivity extends BaseActivity {
    private static final int LOGIN_ACTIVITY_CODE = 0xacc;

    private MenuItem accountMenuItem;
    private OrderHistoryFragment fragment;

    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        fragment = (OrderHistoryFragment) getSupportFragmentManager().findFragmentById(R.id.activity_order_history_fragment);

        getActionBar().setTitle(R.string.activity_order_history);
    }

    //endregion


    //region Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order_history, menu);

        this.accountMenuItem = menu.findItem(R.id.menu_order_history_account);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Ticketing.getTicketService().hasSession())
            accountMenuItem.setTitle(R.string.action_account_logout);
        else
            accountMenuItem.setTitle(R.string.action_account_login);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.menu_order_history_account) {
            if (Ticketing.getTicketService().hasSession())
                logout();
            else
                showAccountActivity();

            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    //endregion


    //region Account Management

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (Ticketing.getTicketService().hasSession())
                    fragment.loadSinglePage(true);
                invalidateOptionsMenu();
            } else {
                finish();
            }
        }
    }

    public void showAccountActivity() {
        startActivityForResult(new Intent(this, AccountActivity.class), LOGIN_ACTIVITY_CODE);
    }

    public void updateActionBar() {
        if (!Ticketing.getTicketService().hasSession()) {
            getActionBar().setSubtitle(null);
        } else {
            User user = Ticketing.getTicketService().getUser();
            if (user != null)
                getActionBar().setSubtitle(user.getEmail());
            else
                getActionBar().setSubtitle(null);
        }
    }

    public void logout() {
        fragment.setRefreshing(false);

        final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(getSupportFragmentManager(), LoadingDialogFragment.TAG);
        final TimedEvent logoutEvent = Ticketing.getAnalytics().startTimedEvent(TicketLibrary.Method.LOGOUT);
        Ticketing.getTicketService().logout(new ResponseListener<TicketResponse>() {
            @Override
            public void onSuccess(int i, TicketResponse ticketResponse) {
                loadingDialogFragment.dismissAllowingStateLoss();

                fragment.loadSinglePage(true);
                invalidateOptionsMenu();

                Ticketing.getAnalytics().finishTimedEvent(logoutEvent);
            }
        }, new BaseResponseListener() {
            @Override
            public void onError(int requestId, int httpStatusCode, Error error) {
                Log.e(getClass().getName(), "Remote log out failed. Ignoring. Error: " + error);
                loadingDialogFragment.dismissAllowingStateLoss();

                // Even if we fail to logout on the server, the local
                // state has been wiped out by the time this callback
                // method is invoked, so we just treat it as a success.
                fragment.loadSinglePage(true);
                invalidateOptionsMenu();

                logoutEvent.updatePropertiesForError(httpStatusCode, error);
                Ticketing.getAnalytics().finishTimedEvent(logoutEvent);
            }

            @Override
            public void onCaptcha(int requestId, Captcha captcha) {
                loadingDialogFragment.dismissAllowingStateLoss();
            }

            @Override
            public void onPolling(int requestId, Polling polling) {
                loadingDialogFragment.dismissAllowingStateLoss();
            }
        });

        OrdersCacheManager.getInstance().clearOfflineCache(this);
    }

    //endregion


    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_ORDERS;
    }
}
