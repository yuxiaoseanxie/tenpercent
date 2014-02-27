/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na2.notifications.ui;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.urbanairship.richpush.RichPushMessage;
import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.notifications.ui.RichPushMessageAdapter.ViewBinder;

import java.text.SimpleDateFormat;

/**
 * Sample implementation of the BaseInboxFragment
 *
 */
public class RichPushInboxFragment extends BaseInboxFragment {

    private static final SimpleDateFormat UA_DATE_FORMATTER = new SimpleDateFormat("EEE MMM d'.' yyyy 'at' h:mm aa");

    @Override
    public int getRowLayoutId() {
        return R.layout.list_inbox_message;
    }

    @Override
    public int getEmptyListStringId() {
        return R.string.no_messages;
    }

    @Override
    protected ViewBinder createMessageBinder() {
        return new RichPushMessageAdapter.ViewBinder() {

            @Override
            public void bindView(View view, final RichPushMessage message) {
                View unreadIndicator = view.findViewById(R.id.unread_indicator);
                TextView title = (TextView) view.findViewById(R.id.title);
                TextView timeStamp = (TextView) view.findViewById(R.id.date_sent);
                final CheckBox checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);

                if (message.isRead()) {
                    unreadIndicator.setBackgroundColor(Color.TRANSPARENT);
                    unreadIndicator.setContentDescription("Message is read");
                } else {
                    unreadIndicator.setBackgroundColor(0xffdd223e);
                    unreadIndicator.setContentDescription("Message is unread");
                }

                title.setText(message.getTitle());
                timeStamp.setText(UA_DATE_FORMATTER.format(message.getSentDate()));

                checkBox.setChecked(isMessageSelected(message.getMessageId()));

                checkBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onMessageSelected(message.getMessageId(), checkBox.isChecked());
                    }
                });
                view.setFocusable(false);
                view.setFocusableInTouchMode(false);
            }
        };
    }
}
