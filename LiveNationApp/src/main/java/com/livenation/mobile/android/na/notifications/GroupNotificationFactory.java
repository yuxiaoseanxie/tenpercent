package com.livenation.mobile.android.na.notifications;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.livenation.mobile.android.na.R;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.DefaultNotificationFactory;
import com.urbanairship.util.UAStringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The main notification will be treated normally and act as the summary notification,
 * while an additional group notification will be posted to the notification center directly.
 */
public class GroupNotificationFactory extends DefaultNotificationFactory {
    final static String GROUP_KEY = "group_key";
    final static int GROUP_ID = 0;
    private static Map<Integer, PushMessage> pushMessageMap = Collections.synchronizedMap(new HashMap<Integer, PushMessage>());

    public GroupNotificationFactory(Context context) {
        super(context);
    }

    @Override
    public Notification createNotification(PushMessage message, int notificationId) {
        // do not display a notification if there is not an alert
        if (UAStringUtil.isEmpty(message.getAlert())) {
            return null;
        }

        // Build the group notification
        String groupAlert = message.getAlert();
        Notification groupNotification = new NotificationCompat.Builder(getContext())
                .setAutoCancel(true)
                .setContentTitle(getContext().getString(R.string.app_name))
                .setContentText(groupAlert)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setGroup(GROUP_KEY)
                .build();

        // Post only the group notification directly to the notification manager
        int id = super.getNextId(message);
        NotificationManagerCompat.from(getContext()).notify(id, groupNotification);

        pushMessageMap.put(id, message);
        // Create an InboxStyle notification
        NotificationCompat.Builder summaryNotificationBuilder = createNotificationBuilder(message, notificationId, new NotificationCompat.BigTextStyle().bigText(message.getAlert()))
                .setContentText(String.valueOf(pushMessageMap.size()) + " new message(s)")
                .setContentTitle(getContext().getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_stat_notify);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (PushMessage push : pushMessageMap.values()) {
            inboxStyle.addLine(push.getAlert());
        }

        // Return the summary notification. This will be posted and tracked by Urban Airship
        return summaryNotificationBuilder.setStyle(inboxStyle)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build();
    }

    @Override
    public int getNextId(PushMessage pushMessage) {
        return GROUP_ID;
    }
}
