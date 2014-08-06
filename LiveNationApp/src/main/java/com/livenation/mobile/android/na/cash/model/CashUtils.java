package com.livenation.mobile.android.na.cash.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class CashUtils {
    public static final String LOG_TAG = "Cash";

    public static final String EXTRA_QUANTITIES = "com.livenation.mobile.android.na.cash.EXTRA_QUANTITIES";
    public static final String EXTRA_CONTACTS = "com.livenation.mobile.android.na.cash.EXTRA_CONTACTS";
    public static final String EXTRA_TOTAL = "com.livenation.mobile.android.na.cash.EXTRA_TOTAL";
    public static final String EXTRA_QUANTITY = "com.livenation.mobile.android.na.cash.EXTRA_QUANTITY";

    public static void dismissKeyboard(@Nullable View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
