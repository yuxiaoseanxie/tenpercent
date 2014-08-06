package com.livenation.mobile.android.na.cash.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.mobilitus.tm.tickets.models.Total;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CashUtils {
    //region Misc Constants

    public static final String LOG_TAG = "Cash";
    public static final String PREFS_ID = "Cash";
    public static final String ACTION_REQUESTS_COMPLETED = "com.livenation.mobile.android.na.cash.ACTION_REQUESTS_COMPLETED";

    //endregion


    //region Activity Extras

    public static final String EXTRA_TICKET_PER_CONTACT_QUANTITIES = "com.livenation.mobile.android.na.cash.EXTRA_TICKET_PER_CONTACT_QUANTITIES";
    public static final String EXTRA_CONTACTS = "com.livenation.mobile.android.na.cash.EXTRA_CONTACTS";
    public static final String EXTRA_TOTAL = "com.livenation.mobile.android.na.cash.EXTRA_TOTAL";
    public static final String EXTRA_TICKET_QUANTITY = "com.livenation.mobile.android.na.cash.EXTRA_TICKET_QUANTITY";
    public static final String EXTRA_CUSTOMER_STATUS = "com.livenation.mobile.android.na.cash.EXTRA_CUSTOMER_STATUS";

    //endregion


    //region Config

    public static final String CLIENT_ID = "a2jqttf932pokmmkp0xtzz8ku";
    public static final String CLIENT_SECRET = "31842a1e8aba240fcc85c20d2ed74f83";
    public static final String SCHEME = "http";
    public static final String AUTHORITY = "cash.square-sandbox.com";

    //endregion


    //region Utilities

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

    //endregion
}
