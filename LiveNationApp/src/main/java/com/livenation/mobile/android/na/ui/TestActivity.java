package com.livenation.mobile.android.na.ui;

import android.app.Activity;
import android.os.Bundle;

import com.livenation.mobile.android.na.apiconfig.ConfigManager;
import com.livenation.mobile.android.na.app.LiveNationApplication;

/**
 * Created by cchilton on 3/10/14.
 */
public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigManager configManager = LiveNationApplication.get().getConfigManager();

        LiveNationApplication.get().getConfigManager().buildApi();
    }
}
