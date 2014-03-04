/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na2.notifications.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.app.Constants;
import com.urbanairship.Logger;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.widget.RichPushMessageView;

import java.text.SimpleDateFormat;

/**
 * Fragment that displays a rich push activity_message in a RichPushMessageView
 */
public class MessageFragment extends Fragment {
    private static final SimpleDateFormat DATE_RECEIVED_FORMAT = new SimpleDateFormat("MMM d, yyyy  h:mm a");

    private RichPushMessage message;
    private RichPushMessageView browser;
    private TextView subjectText;
    private TextView dateReceivedText;
    private Button callToActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container);

        browser = (RichPushMessageView)view.findViewById(R.id.fragment_message_view);
        subjectText = (TextView)view.findViewById(R.id.fragment_message_subject);
        dateReceivedText = (TextView)view.findViewById(R.id.fragment_message_date);
        callToActionButton = (Button)view.findViewById(R.id.fragment_message_cta_button);
        callToActionButton.setOnClickListener(new CallToActionClickListener());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String messageId = getActivity().getIntent().getStringExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY);
        message = RichPushManager.shared().getRichPushUser().getInbox().getMessage(messageId);

        if (message != null) {
            subjectText.setText(message.getTitle());
            dateReceivedText.setText(DATE_RECEIVED_FORMAT.format(message.getSentDate()));

            Bundle messageExtras = message.getExtras();
            if(messageExtras.containsKey(Constants.Notifications.EXTRA_MESSAGE_ACTION_URL)) {
                String title = messageExtras.getString(Constants.Notifications.EXTRA_MESSAGE_ACTION_NAME,
                                                       getString(R.string.message_cta_default));
                callToActionButton.setText(title);
            } else {
                callToActionButton.setVisibility(View.GONE);
            }

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


    private class CallToActionClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Bundle messageExtras = message.getExtras();
            if(!messageExtras.containsKey(Constants.Notifications.EXTRA_MESSAGE_ACTION_URL)) {
                throw new IllegalStateException("CallToActionClickListener.onClick should never be called without an EXTRA_MESSAGE_ACTION_URL");
            }

            String url = messageExtras.getString(Constants.Notifications.EXTRA_MESSAGE_ACTION_URL);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }
}
