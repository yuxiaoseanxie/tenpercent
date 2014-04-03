package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

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
}
