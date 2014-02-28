/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na2.notifications.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.livenation.mobile.android.na2.app.Constants;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.urbanairship.richpush.RichPushMessage;
import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.notifications.ui.RichPushMessageAdapter.ViewBinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Sample implementation of the BaseInboxFragment
 *
 */
public class RichPushInboxFragment extends BaseInboxFragment {

    private static final SimpleDateFormat INCOMING_FORMAT = new SimpleDateFormat(LiveNationApiService.LOCAL_START_TIME_FORMAT, Locale.US);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E', 'MMM' 'dd", Locale.US);

    private int getMessageType(RichPushMessage message)
    {
        Bundle extras = message.getExtras();
        if(extras.containsKey(Constants.Notifications.EXTRA_TYPE)) {
            Object type = extras.get(Constants.Notifications.EXTRA_TYPE);
            if(type instanceof String) {
                return Integer.valueOf((String)type);
            } else if(type instanceof Integer) {
                return (Integer)type;
            } else {
                return Constants.Notifications.TYPE_FEATURED_CONTENT;
            }
        } else {
            return Constants.Notifications.TYPE_FEATURED_CONTENT;
        }
    }

    private boolean isMessageForEvent(RichPushMessage message)
    {
        int type = getMessageType(message);
        return (type == Constants.Notifications.TYPE_EVENT_ON_SALE_NOW ||
                type == Constants.Notifications.TYPE_EVENT_ANNOUNCEMENT ||
                type == Constants.Notifications.TYPE_EVENT_LAST_MINUTE ||
                type == Constants.Notifications.TYPE_EVENT_MOBILE_PRESALE);
    }

    private boolean isMessageInVenue(RichPushMessage message)
    {
        return (getMessageType(message) == Constants.Notifications.TYPE_IN_VENUE);
    }


    private String getNameForType(int type)
    {
        switch (type) {
            case Constants.Notifications.TYPE_EVENT_ON_SALE_NOW:
                return "ON SALE";

            case Constants.Notifications.TYPE_EVENT_ANNOUNCEMENT:
                return "ON SALE";

            case Constants.Notifications.TYPE_EVENT_LAST_MINUTE:
                return "LAST MINUTE";

            case Constants.Notifications.TYPE_EVENT_MOBILE_PRESALE:
                return "PRESALE";

            case Constants.Notifications.TYPE_IN_VENUE:
                return "LIVE FEED";

            case Constants.Notifications.TYPE_FEATURED_CONTENT:
            default:
                return "FEATURED";
        }
    }

    private String getMessageInfoText(RichPushMessage message)
    {
        if(isMessageForEvent(message)) {
            Bundle extra = message.getExtras();

            int type = getMessageType(message);
            String localizedName = getNameForType(type);
            Date onSaleDate = null;
            try {
                onSaleDate = INCOMING_FORMAT.parse(extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_ON_SALE_DATE));
            } catch (ParseException e) {
                Log.e("Notification Parse Errors", "Malformed date passed through. " + e.toString());
            }

            switch (type) {
                case Constants.Notifications.TYPE_EVENT_ANNOUNCEMENT: {
                    String onSaleString = (onSaleDate != null)? DATE_FORMAT.format(onSaleDate) : null;
                    if(onSaleString == null)
                        onSaleString = "NOW";
                    return (localizedName + " - " + onSaleString);
                }

                case Constants.Notifications.TYPE_EVENT_ON_SALE_NOW:
                case Constants.Notifications.TYPE_EVENT_MOBILE_PRESALE: {
                    String onSaleString = TIME_FORMAT.format(onSaleDate);
                    if(onSaleString == null)
                        onSaleString = "NOW";
                    return (localizedName  + " - TODAY " + onSaleString);
                }

                default:
                    return localizedName;
            }
        } else if(isMessageInVenue(message)) {
            String artistName = message.getExtras().getString(Constants.Notifications.EXTRA_EVENT_INFO_ARTIST_NAME);
            return (getNameForType(Constants.Notifications.TYPE_IN_VENUE) + " - " + artistName);
        } else {
            return getNameForType(Constants.Notifications.TYPE_FEATURED_CONTENT);
        }
    }

    private String getMessageTitleText(RichPushMessage message)
    {
        if(isMessageForEvent(message)) {
            Bundle extras = message.getExtras();
            if(extras.containsKey(Constants.Notifications.EXTRA_EVENT_INFO_ARTIST_NAME))
                return extras.getString(Constants.Notifications.EXTRA_EVENT_INFO_ARTIST_NAME);
            else
                return message.getTitle();
        } else {
            return message.getTitle();
        }
    }

    private String getMessageDetailText(RichPushMessage message)
    {
        if(isMessageForEvent(message)) {
            Bundle extra = message.getExtras();
            Date startTime = null;
            try {
                startTime = INCOMING_FORMAT.parse(extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_LOCAL_START_TIME));
            } catch (ParseException e) {
                Log.e("Notification Parse Errors", "Malformed date passed through. " + e.toString());
            }
            String startTimeString = (startTime != null)? DATE_FORMAT.format(startTime) : null;
            return (startTimeString + " @ " + extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_VENUE_NAME));
        } else {
            return "";
        }
    }

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
                TextView info = (TextView) view.findViewById(R.id.message_info);
                TextView title = (TextView) view.findViewById(R.id.message_title);
                TextView details = (TextView) view.findViewById(R.id.message_details);
                final CheckBox checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);

                if (message.isRead()) {
                    unreadIndicator.setBackgroundColor(Color.TRANSPARENT);
                    unreadIndicator.setContentDescription("Message is read");
                } else {
                    unreadIndicator.setBackgroundColor(0xffdd223e);
                    unreadIndicator.setContentDescription("Message is unread");
                }

                info.setText(getMessageInfoText(message));
                title.setText(getMessageTitleText(message));
                details.setText(getMessageDetailText(message));

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
