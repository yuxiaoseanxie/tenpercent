package com.livenation.mobile.android.na.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.segment.android.Logger;

/**
 * Created by elodieferrais on 4/11/14.
 */
public class ContactUtils {
    public static void dial(String phoneNumber, Activity activity) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber.trim()));
            activity.startActivity(callIntent);
        } catch (ActivityNotFoundException e) {
            Logger.e(activity.getClass().getSimpleName() + " Call failed", e);
        }
    }

    public static void emailTo(String emailAddress, String subject, String message, Activity activity) {

        //This solution is the only one which work for every platforms
        //If we set the uri after instantiate the intent, it won't work on 4.1.2
        Intent it = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
        it.putExtra(Intent.EXTRA_SUBJECT, subject);
        it.putExtra(Intent.EXTRA_TEXT, message);
        activity.startActivity(it);
    }
}
