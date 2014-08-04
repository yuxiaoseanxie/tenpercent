package com.livenation.mobile.android.na.cash;

import android.os.Bundle;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;

public class CashRecipientsActivity extends LiveNationFragmentActivity {
    //region Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_recipients);
    }

    //endregion
}
