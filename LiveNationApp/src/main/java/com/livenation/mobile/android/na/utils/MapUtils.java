package com.livenation.mobile.android.na.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;
import java.util.Locale;

/**
 * Created by elodieferrais on 4/24/14.
 */
public class MapUtils {
    static public void redirectToMapApplication(double lat, double lng, Context context) {

        String uri = String.format(Locale.ENGLISH, "geo:%f,%f", lat, lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
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
