package com.livenation.mobile.android.na.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;

/**
 * Created by elodieferrais on 4/24/14.
 */
public class MapUtils {
    static public void redirectToMapApplication(double lat, double lng, String address, Context context) {

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + address + ")"));
        intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final PackageManager pm = context.getPackageManager();
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);

        if (!apps.isEmpty()) {
            context.startActivity(intent);
        } else {
            //TODO https://www.pivotaltracker.com/story/show/70134186
        }
    }
}
