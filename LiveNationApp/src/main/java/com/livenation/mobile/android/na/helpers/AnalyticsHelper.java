package com.livenation.mobile.android.na.helpers;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * Created by elodieferrais on 3/25/14.
 */
public class AnalyticsHelper {

    public static boolean isAppInstalled(String uri, Context context) {
        boolean output = false;
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(uri);
        if (intent != null) {
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            output = list.size() > 0;
        }
        return output;
    }

    public static Props getPropsForEvent(Event event) {
        Props props = new Props();
        String eventName = event.getName();
        if (eventName == null || eventName.isEmpty()) {
            eventName = event.getDisplayName();
        }
        String artistName;
        if (event.getLineup() != null && !event.getLineup().isEmpty()) {
            artistName = event.getLineup().get(0).getName();
        } else {
            artistName = "To Be Announced";
        }
        props.put(AnalyticConstants.EVENT_NAME, eventName);
        props.put(AnalyticConstants.EVENT_ID, event.getId());
        props.put(AnalyticConstants.ARTIST_NAME, artistName);
        props.put(AnalyticConstants.VENUE_NAME, event.getVenue().getName());
        return props;
    }
}
