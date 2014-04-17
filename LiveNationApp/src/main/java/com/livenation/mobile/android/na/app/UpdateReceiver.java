package com.livenation.mobile.android.na.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by elodieferrais on 4/17/14.
 */
public class UpdateReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ELODIE", "updateReceiver has been called");
        Intent newIntent = new Intent(context, TestActivity.class);
        context.startActivity(newIntent);

    }
}
