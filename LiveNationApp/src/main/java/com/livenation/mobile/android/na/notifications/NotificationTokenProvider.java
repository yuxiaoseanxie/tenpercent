package com.livenation.mobile.android.na.notifications;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.urbanairship.UAirship;

/**
 * Created by elodieferrais on 3/23/15.
 */
public class NotificationTokenProvider implements Ticketing.PushTokenProvider {
    @Override
    public String getPushToken() {
        if (BuildConfig.DEBUG)
            return null;
        else
            return UAirship.shared().getPushManager().getChannelId();
    }
}
