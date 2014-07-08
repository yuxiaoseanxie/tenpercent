/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na.notifications.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.urbanairship.UAirship;

/**
 * Manages the activity_message view pager and display messages
 */
public class MessageActivity extends LiveNationFragmentActivity {

    public static final String EXTRA_MESSAGE_ID_KEY = "com.livenation.mobile.android.na.notifications.EXTRA_MESSAGE_ID_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_message);
        getActionBar().setTitle(R.string.message_title);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        Intent intent = new Intent(this, InboxActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

        this.finish();
        return true;
    }

}
