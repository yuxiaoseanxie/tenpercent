/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na.notifications.ui;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.urbanairship.Logger;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.widget.RichPushMessageView;

/**
 * Fragment that displays a rich push activity_message in a RichPushMessageView
 */
public class MessageFragment extends Fragment {
    private RichPushMessageView browser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container);

        browser = (RichPushMessageView)view.findViewById(R.id.fragment_message_view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String messageId = getActivity().getIntent().getStringExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY);
        RichPushMessage message = RichPushManager.shared().getRichPushUser().getInbox().getMessage(messageId);

        if (message != null) {
            browser.loadRichPushMessage(message);
        } else {
            Logger.info("Couldn't retrieve activity_message for ID: " + messageId);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= 11) {
            browser.onPause();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 11) {
            browser.onResume();
        }
    }
}
