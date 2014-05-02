package com.livenation.mobile.android.na.ui;

import android.app.Activity;
import android.os.Bundle;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.ApiHelper;

/**
 * Created by cchilton on 3/10/14.
 */
public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiHelper apiHelper = LiveNationApplication.get().getApiHelper();

        LiveNationApplication.get().getApiHelper().buildApi();
    }
}
