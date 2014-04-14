package com.livenation.mobile.android.na.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import io.segment.android.Logger;

/**
 * Created by elodieferrais on 4/11/14.
 */
public class PhoneUtils {
    public static void dial(String phoneNumber, Activity activity) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber.trim()));
            activity.startActivity(callIntent);
        } catch (ActivityNotFoundException e) {
            Logger.e(activity.getClass().getSimpleName() + " Call failed", e);
        }
    }
}
