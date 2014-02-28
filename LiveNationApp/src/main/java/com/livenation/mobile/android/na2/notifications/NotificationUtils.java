package com.livenation.mobile.android.na2.notifications;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.urbanairship.richpush.RichPushMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by km on 2/27/14.
 */
public class NotificationUtils {
    public static final String EXTRA_TYPE = "type";

    public static final int TYPE_FEATURED_CONTENT = -1;
    public static final int TYPE_EVENT_ON_SALE_NOW = 0;
    public static final int TYPE_EVENT_ANNOUNCEMENT = 1;
    public static final int TYPE_EVENT_LAST_MINUTE = 2;
    public static final int TYPE_EVENT_MOBILE_PRESALE = 3;
    public static final int TYPE_IN_VENUE = 4;


    public static final String EXTRA_ENTITY_ID = "id";

    public static final String EXTRA_MESSAGE_ACTION_URL = "message_action_url";
    public static final String EXTRA_MESSAGE_ACTION_NAME = "message_action_name";

    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_VIDEO_URL = "video_url";

    public static final String EXTRA_EVENT_INFO_ON_SALE_DATE = "on_sale_date";
    public static final String EXTRA_EVENT_INFO_ARTIST_NAME = "artist_name";
    public static final String EXTRA_EVENT_INFO_VENUE_NAME = "venue_name";
    public static final String EXTRA_EVENT_INFO_LOCAL_START_TIME = "local_start_time";

    public static final String EXTRA_RICH_MESSAGE_ID = "_uamid";



    private static final SimpleDateFormat INCOMING_FORMAT = new SimpleDateFormat(LiveNationApiService.LOCAL_START_TIME_FORMAT, Locale.US);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("E', 'MMM' 'dd", Locale.US);

    public static int getMessageType(RichPushMessage message)
    {
        Bundle extras = message.getExtras();
        if(extras.containsKey(EXTRA_TYPE)) {
            Object type = extras.get(EXTRA_TYPE);
            if(type instanceof String) {
                return Integer.valueOf((String)type);
            } else if(type instanceof Integer) {
                return (Integer)type;
            } else {
                return TYPE_FEATURED_CONTENT;
            }
        } else {
            return TYPE_FEATURED_CONTENT;
        }
    }

    public static boolean isMessageForEvent(RichPushMessage message)
    {
        int type = getMessageType(message);
        return (type == TYPE_EVENT_ON_SALE_NOW ||
                type == TYPE_EVENT_ANNOUNCEMENT ||
                type == TYPE_EVENT_LAST_MINUTE ||
                type == TYPE_EVENT_MOBILE_PRESALE);
    }

    public static boolean isMessageInVenue(RichPushMessage message)
    {
        return (getMessageType(message) == TYPE_IN_VENUE);
    }


    public static String getNameForType(Context context, int type)
    {
        switch (type) {
            case TYPE_EVENT_ON_SALE_NOW:
                return "ON SALE";

            case TYPE_EVENT_ANNOUNCEMENT:
                return "ON SALE";

            case TYPE_EVENT_LAST_MINUTE:
                return "LAST MINUTE";

            case TYPE_EVENT_MOBILE_PRESALE:
                return "PRESALE";

            case TYPE_IN_VENUE:
                return "LIVE FEED";

            case TYPE_FEATURED_CONTENT:
            default:
                return "FEATURED";
        }
    }

    public static String getMessageInfoText(Context context, RichPushMessage message)
    {
        if(isMessageForEvent(message)) {
            Bundle extra = message.getExtras();

            int type = getMessageType(message);
            String localizedName = getNameForType(context, type);
            Date onSaleDate = null;
            try {
                onSaleDate = INCOMING_FORMAT.parse(extra.getString(EXTRA_EVENT_INFO_ON_SALE_DATE));
            } catch (ParseException e) {
                Log.e("Notification Parse Errors", "Malformed date passed through. " + e.toString());
            }

            switch (type) {
                case TYPE_EVENT_ANNOUNCEMENT: {
                    String onSaleString = (onSaleDate != null)? DATE_FORMAT.format(onSaleDate) : null;
                    if(onSaleString == null)
                        onSaleString = "NOW";
                    return (localizedName + " - " + onSaleString);
                }

                case TYPE_EVENT_ON_SALE_NOW:
                case TYPE_EVENT_MOBILE_PRESALE: {
                    String onSaleString = TIME_FORMAT.format(onSaleDate);
                    if(onSaleString == null)
                        onSaleString = "NOW";
                    return (localizedName  + " - TODAY " + onSaleString);
                }

                default:
                    return localizedName;
            }
        } else if(isMessageInVenue(message)) {
            String artistName = message.getExtras().getString(EXTRA_EVENT_INFO_ARTIST_NAME);
            return (getNameForType(context, TYPE_IN_VENUE) + " - " + artistName);
        } else {
            return getNameForType(context, TYPE_FEATURED_CONTENT);
        }
    }

    public static String getMessageTitleText(Context context, RichPushMessage message)
    {
        if(isMessageForEvent(message)) {
            Bundle extras = message.getExtras();
            if(extras.containsKey(EXTRA_EVENT_INFO_ARTIST_NAME))
                return extras.getString(EXTRA_EVENT_INFO_ARTIST_NAME);
            else
                return message.getTitle();
        } else {
            return message.getTitle();
        }
    }

    public static String getMessageDetailText(Context context, RichPushMessage message)
    {
        if(isMessageForEvent(message)) {
            Bundle extra = message.getExtras();
            Date startTime = null;
            try {
                startTime = INCOMING_FORMAT.parse(extra.getString(EXTRA_EVENT_INFO_LOCAL_START_TIME));
            } catch (ParseException e) {
                Log.e("Notification Parse Errors", "Malformed date passed through. " + e.toString());
            }
            String startTimeString = (startTime != null)? DATE_FORMAT.format(startTime) : null;
            return (startTimeString + " @ " + extra.getString(EXTRA_EVENT_INFO_VENUE_NAME));
        } else {
            return "";
        }
    }
}
