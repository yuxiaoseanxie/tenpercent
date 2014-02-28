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
import com.livenation.mobile.android.platform.util.Logger;
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
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("E', 'MMM' 'dd", Locale.US);
    private static final SimpleDateFormat LONG_DATE_FORMAT = new SimpleDateFormat("E', 'MMM' 'dd' at 'h:mm a", Locale.US);

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

    private Date parseDateString(SimpleDateFormat formatter, String dateTimeString)
    {
        Date date;
        try {
            date = formatter.parse(dateTimeString);
        } catch (ParseException e) {
            date = new Date(1041509106000L);

            Logger.log("Notification Date Parse Errors", "Malformed date passed through. Using default.", e);
        }

        return date;
    }

    private String getNameForType(int type)
    {
        switch (type) {
            case Constants.Notifications.TYPE_EVENT_ON_SALE_NOW:
                return getString(R.string.notif_type_on_sale);

            case Constants.Notifications.TYPE_EVENT_ANNOUNCEMENT:
                return getString(R.string.notif_type_announcement);

            case Constants.Notifications.TYPE_EVENT_LAST_MINUTE:
                return getString(R.string.notif_type_last_minute);

            case Constants.Notifications.TYPE_EVENT_MOBILE_PRESALE:
                return getString(R.string.notif_type_presale);

            case Constants.Notifications.TYPE_IN_VENUE:
                return getString(R.string.notif_type_in_venue);

            case Constants.Notifications.TYPE_FEATURED_CONTENT:
            default:
                return getString(R.string.notif_type_featured);
        }
    }

    private String getMessageInfoText(RichPushMessage message)
    {
        if(isMessageForEvent(message)) {
            Bundle extra = message.getExtras();

            int type = getMessageType(message);
            String localizedName = getNameForType(type);
            Date onSaleDate = parseDateString(INCOMING_FORMAT, extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_ON_SALE_DATE));

            switch (type) {
                case Constants.Notifications.TYPE_EVENT_ANNOUNCEMENT: {
                    String onSaleString = (onSaleDate != null)? SHORT_DATE_FORMAT.format(onSaleDate) : null;
                    if(onSaleString == null)
                        onSaleString = getString(R.string.notif_date_now);
                    return String.format(getString(R.string.notif_event_info_format), localizedName, onSaleString);
                }

                case Constants.Notifications.TYPE_EVENT_ON_SALE_NOW:
                case Constants.Notifications.TYPE_EVENT_MOBILE_PRESALE: {
                    String onSaleString = LONG_DATE_FORMAT.format(onSaleDate);
                    if(onSaleString == null)
                        onSaleString = getString(R.string.notif_date_now);
                    return String.format(getString(R.string.notif_event_info_format), localizedName, onSaleString);
                }

                default:
                    return localizedName;
            }
        } else if(isMessageInVenue(message)) {
            String typeName = getNameForType(Constants.Notifications.TYPE_IN_VENUE);
            String artistName = message.getExtras().getString(Constants.Notifications.EXTRA_EVENT_INFO_ARTIST_NAME);
            return String.format(getString(R.string.notif_event_info_format), typeName, artistName);
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
            Date startTime = parseDateString(INCOMING_FORMAT, extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_LOCAL_START_TIME));
            String startTimeString = (startTime != null)? SHORT_DATE_FORMAT.format(startTime) : null;
            String venueName = extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_VENUE_NAME);
            return String.format(getString(R.string.notif_event_details_format), startTimeString, venueName);
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
