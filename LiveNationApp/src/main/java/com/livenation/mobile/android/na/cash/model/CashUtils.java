package com.livenation.mobile.android.na.cash.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.cash.ui.CashRecipientsActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.init.Environment;
import com.mobilitus.tm.tickets.models.Total;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CashUtils {
    //region Misc Constants

    public static final String LOG_TAG = "Cash";
    public static final String PREFS_ID = "Cash";
    public static final String ACTION_REQUESTS_COMPLETED = "com.livenation.mobile.android.na.cash.ACTION_REQUESTS_COMPLETED";
    public static final String HELP_URL = "https://squareup.com/help/en-us/topic/139-square-cash";

    //endregion


    //region Activity Extras

    public static final String EXTRA_EVENT = "com.livenation.mobile.android.na.cash.EXTRA_EVENT";
    public static final String EXTRA_TICKET_PER_CONTACT_QUANTITIES = "com.livenation.mobile.android.na.cash.EXTRA_TICKET_PER_CONTACT_QUANTITIES";
    public static final String EXTRA_CONTACTS = "com.livenation.mobile.android.na.cash.EXTRA_CONTACTS";
    public static final String EXTRA_TOTAL = "com.livenation.mobile.android.na.cash.EXTRA_TOTAL";
    public static final String EXTRA_TICKET_QUANTITY = "com.livenation.mobile.android.na.cash.EXTRA_TICKET_QUANTITY";
    public static final String EXTRA_USED_TICKET_QUANTITY = "com.livenation.mobile.android.na.cash.EXTRA_USED_TICKET_QUANTITY";
    public static final String EXTRA_CUSTOMER_STATUS = "com.livenation.mobile.android.na.cash.EXTRA_CUSTOMER_STATUS";
    public static final String EXTRA_NOTE = "com.livenation.mobile.android.na.cash.EXTRA_NOTE";

    //endregion


    //region Utilities

    public static String getHostForEnvironment(Environment environment) {
        switch (environment) {
            default:
            case Production:
            case ProductionDirect:
                return "connect.squareup.com";

            case Staging:
            case StagingDirect:
            case Integration:
                return "cash.square-sandbox.com";
        }
    }

    public static void dismissKeyboard(@Nullable View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public static long calculatePricePerTicket(@NonNull Total total, long quantity) {
        BigDecimal pricePerTicket = total.getGrandTotal().divide(BigDecimal.valueOf(quantity), RoundingMode.HALF_EVEN);
        return pricePerTicket.longValue() * 100;
    }

    public static SharedPreferences getPreferences() {
        return LiveNationApplication.get().getSharedPreferences(PREFS_ID, 0);
    }

    public static void showHelpWebsite(@NonNull Context fromContext) {
        fromContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HELP_URL)));
    }

    //endregion


    public static void startPaybackFlow(@NonNull FragmentActivity activity,
                                        @NonNull Total total,
                                        int ticketQuantity,
                                        @NonNull Event event) {
        Intent intent = new Intent(activity, CashRecipientsActivity.class);
        intent.putExtra(EXTRA_TOTAL, total);
        intent.putExtra(EXTRA_TICKET_QUANTITY, ticketQuantity);
        intent.putExtra(EXTRA_EVENT, event);
        activity.startActivity(intent);
    }
}
