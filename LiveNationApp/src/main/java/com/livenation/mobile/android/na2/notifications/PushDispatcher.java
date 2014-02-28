package com.livenation.mobile.android.na2.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.livenation.mobile.android.na2.notifications.ui.InboxActivity;
import com.livenation.mobile.android.na2.notifications.ui.MessageActivity;
import com.livenation.mobile.android.na2.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na2.ui.ShowActivity;
import com.urbanairship.richpush.RichPushMessage;

/**
 * Created by km on 2/27/14.
 */
public class PushDispatcher {
    public static final String TYPE = "type";
    public static final String ENTITY_ID = "id";

    public static final String MESSAGE_ACTION_URL = "message_action_url";
    public static final String MESSAGE_ACTION_NAME = "message_action_name";

    public static final String IMAGE_URL = "image_url";
    public static final String VIDEO_URL = "video_url";

    public static final String EVENT_INFO_ON_SALE_DATE = "on_sale_date";
    public static final String EVENT_INFO_ARTIST_NAME = "artist_name";
    public static final String EVENT_INFO_VENUE_NAME = "venue_name";
    public static final String EVENT_INFO_LOCALSTARTTIME = "local_start_time";

    public static final String MESSAGE_RICH_ID = "_uamid";

    public static void dispatch(Context context, Intent intent)
    {
        Intent outgoingIntent;
        if(intent.hasExtra(ENTITY_ID)) {
            outgoingIntent = new Intent(context, ShowActivity.class);
            outgoingIntent.putExtra(SingleEventPresenter.PARAMETER_EVENT_ID, intent.getStringExtra(ENTITY_ID));
        } else {
            String messageId = intent.getStringExtra(PushDispatcher.MESSAGE_RICH_ID);
            outgoingIntent = new Intent(context, InboxActivity.class);
            outgoingIntent.putExtra(InboxActivity.MESSAGE_ID_RECEIVED_KEY, messageId);
        }
        outgoingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(outgoingIntent);
    }
}
