/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na.notifications.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.notifications.ui.RichPushMessageAdapter.ViewBinder;
import com.urbanairship.richpush.RichPushMessage;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Sample implementation of the BaseInboxFragment
 */
public class RichPushInboxFragment extends BaseInboxFragment implements AdapterView.OnItemLongClickListener {
    //Dont use Java6's non ISO8601 compliant (Doesn't handle 'Z' timezone) SimpleDateFormat for incoming format
    private static final DateTimeFormatter INCOMING_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("E', 'MMM' 'dd", Locale.US);
    private static final SimpleDateFormat LONG_DATE_FORMAT = new SimpleDateFormat("E', 'MMM' 'dd' at 'h:mm a", Locale.US);

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnItemLongClickListener(this);
    }

    public int getMessageType(RichPushMessage message) {
        Bundle extras = message.getExtras();
        if (extras.containsKey(Constants.Notifications.EXTRA_TYPE)) {
            String typeString = extras.getString(Constants.Notifications.EXTRA_TYPE);
            return Integer.valueOf(typeString);
        } else {
            return Constants.Notifications.TYPE_FEATURED_CONTENT;
        }
    }

    private boolean isMessageForEvent(RichPushMessage message) {
        int type = getMessageType(message);
        return (type == Constants.Notifications.TYPE_EVENT_ON_SALE_NOW ||
                type == Constants.Notifications.TYPE_EVENT_ANNOUNCEMENT ||
                type == Constants.Notifications.TYPE_EVENT_LAST_MINUTE ||
                type == Constants.Notifications.TYPE_EVENT_MOBILE_PRESALE);
    }

    private boolean isMessageInVenue(RichPushMessage message) {
        return (getMessageType(message) == Constants.Notifications.TYPE_IN_VENUE);
    }

    private Date parseDateString(DateTimeFormatter formatter, String dateTimeString) {
        Date date;
        try {
            date = formatter.parseDateTime(dateTimeString).toDate();
        } catch (NullPointerException e) {
            date = new Date(1041509106000L /* 01/02/2003 04:05:06 */);
        }

        return date;
    }

    private String getNameForType(int type) {
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

    private String getMessageInfoText(RichPushMessage message) {
        if (isMessageForEvent(message)) {
            Bundle extra = message.getExtras();

            int type = getMessageType(message);
            String localizedName = getNameForType(type);
            Date onSaleDate = parseDateString(INCOMING_FORMAT, extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_ON_SALE_DATE));

            switch (type) {
                case Constants.Notifications.TYPE_EVENT_ANNOUNCEMENT:
                case Constants.Notifications.TYPE_EVENT_ON_SALE_NOW:
                case Constants.Notifications.TYPE_EVENT_MOBILE_PRESALE: {
                    String onSaleString = LONG_DATE_FORMAT.format(onSaleDate);
                    if (onSaleString == null)
                        onSaleString = getString(R.string.notif_date_now);
                    return String.format(getString(R.string.notif_event_info_format), localizedName, onSaleString);
                }

                default:
                    return localizedName;
            }
        } else if (isMessageInVenue(message)) {
            String typeName = getNameForType(Constants.Notifications.TYPE_IN_VENUE);
            String artistName = message.getExtras().getString(Constants.Notifications.EXTRA_EVENT_INFO_ARTIST_NAME);
            return String.format(getString(R.string.notif_event_info_format), typeName, artistName);
        } else {
            return getNameForType(Constants.Notifications.TYPE_FEATURED_CONTENT);
        }
    }

    private String getMessageTitleText(RichPushMessage message) {
        if (isMessageForEvent(message)) {
            Bundle extras = message.getExtras();
            if (extras.containsKey(Constants.Notifications.EXTRA_EVENT_INFO_ARTIST_NAME))
                return extras.getString(Constants.Notifications.EXTRA_EVENT_INFO_ARTIST_NAME);
            else
                return message.getTitle();
        } else {
            return message.getTitle();
        }
    }

    private String getMessageDetailText(RichPushMessage message) {
        if (isMessageForEvent(message)) {
            Bundle extra = message.getExtras();
            Date startTime = parseDateString(INCOMING_FORMAT, extra.getString(Constants.Notifications.EXTRA_EVENT_INFO_LOCAL_START_TIME));
            String startTimeString = (startTime != null) ? SHORT_DATE_FORMAT.format(startTime) : null;
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
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                if (viewHolder == null) {
                    viewHolder = new ViewHolder(view);
                    view.setTag(viewHolder);
                }
                if (message.isRead()) {
                    viewHolder.unreadIndicator.setBackgroundColor(Color.TRANSPARENT);
                    viewHolder.unreadIndicator.setContentDescription("Message is read");
                } else {
                    viewHolder.unreadIndicator.setBackgroundColor(getResources().getColor(R.color.list_show_date_text_highlight));
                    viewHolder.unreadIndicator.setContentDescription("Message is unread");
                }

                viewHolder.info.setText(getMessageInfoText(message));
                viewHolder.title.setText(getMessageTitleText(message));
                viewHolder.details.setText(getMessageDetailText(message));

                viewHolder.checkBox.setChecked(isMessageSelected(message.getMessageId()));

                final CheckBox checkBox = viewHolder.checkBox;
                viewHolder.checkBox.setOnClickListener(new OnClickListener() {
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        RichPushMessage message = (RichPushMessage) getListAdapter().getItem(position);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);
        checkBox.setChecked(!checkBox.isChecked());
        onMessageSelected(message.getMessageId(), checkBox.isChecked());
        return true;
    }

    private class ViewHolder {
        final View unreadIndicator;
        final TextView info;
        final TextView title;
        final TextView details;
        final CheckBox checkBox;

        public ViewHolder(View view) {
            this.unreadIndicator = view.findViewById(R.id.unread_indicator);
            this.info = (TextView) view.findViewById(R.id.message_info);
            this.title = (TextView) view.findViewById(R.id.message_title);
            this.details = (TextView) view.findViewById(R.id.message_details);
            this.checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);
        }
    }
}
