package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.util.List;

import io.segment.android.models.Props;

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
        if (eventName != null || eventName.isEmpty()) {
            eventName = event.getDisplayName();
        }
        String artistName;
        if (event.getLineup() != null && !event.getLineup().isEmpty()) {
            artistName = event.getLineup().get(0).getName();
        } else {
            artistName = "To Be Announced";
        }
        props.put("Event Name", eventName);
        props.put("Artist Name", artistName);
        props.put("Venue Name", event.getVenue().getName());
        return props;
    }
}
