package com.livenation.mobile.android.na.utils;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.proxy.LiveNationConfig;
import com.livenation.mobile.android.platform.api.proxy.ProviderManager;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AppInitData;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;

import java.util.Map;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.net.Uri;
import android.os.Build;

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
            LiveNationAnalytics.logTrace(activity.getClass().getSimpleName() + " Call failed", e.getMessage());
        }
    }

    public static void emailTo(String emailAddress, String subject, String message, Context context) {

        //This solution is the only one which work for every platforms
        //If we set the uri after instantiate the intent, it won't work on 4.1.2
        Intent it = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
        it.putExtra(Intent.EXTRA_SUBJECT, subject);
        it.putExtra(Intent.EXTRA_TEXT, message);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(it);
    }

    public static void buildAndOpenContactUsEmail(final Context context) {

        final String emailAddress = context.getString(R.string.contact_email_address);
        final String subject = context.getString(R.string.contact_email_subject);
        final String message = "\n\n" + context.getString(R.string.contact_email_signature_message)
                + context.getString(R.string.contact_email_signature_message_appversion) + BuildConfig.VERSION_NAME
                + context.getString(R.string.contact_email_signature_message_device) + Build.MANUFACTURER + "  " + Build.MODEL
                + context.getString(R.string.contact_email_signature_message_platform) + Build.VERSION.SDK_INT;
        ProviderManager providerManager = new ProviderManager();
        providerManager.getConfigReadyFor(new ConfigCallback() {
            @Override
            public void onResponse(LiveNationConfig response) {
                Map<String, String> userInfo = response.getAppInitResponse().getData().getUserInfo();
                String userId = userInfo.get(AppInitData.USER_INFO_ID_KEY);
                String signature = message + context.getString(R.string.contact_email_signature_message_userid) + userId;
                ContactUtils.emailTo(emailAddress, subject, signature, context);
            }

            @Override
            public void onErrorResponse(int errorCode) {
                ContactUtils.emailTo(emailAddress, subject, message, context);
            }
        }, ProviderManager.ProviderType.APP_INIT);
    }

}
