package com.livenation.mobile.android.na.utils;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.providers.ConfigFileProvider;
import com.livenation.mobile.android.na.ui.OrderConfirmationActivity;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.ticketing.Ticketing;

import java.util.Calendar;
import java.util.List;

/**
 * Created by elodieferrais on 10/20/14.
 */
public class EventUtils {

    static private boolean isSDPAvoidable(Event event, ConfigFileProvider.ConfigFile configFile, Context context) {
        List<TicketOffering> offerings = event.getTicketOfferings();

        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.AB_TESTING, Context.MODE_PRIVATE);
        boolean isNewUser = sharedPreferences.getBoolean(Constants.SharedPreferences.IS_NEW_USER, true);

        return (event.getOnSaleDate() != null
                && event.getOnSaleDate().compareTo(Calendar.getInstance().getTime()) <= 0
                && offerings.size() == 1
                && Ticketing.isTicketmasterUrl(offerings.get(0).getPurchaseUrl())
                && LiveNationApplication.get().getInstalledAppConfig().isCommerceAvailable()
                && configFile.skipSDPFeature
                && isNewUser);
    }

    static private void redirectToSDP(Context context, Event event) {
        Intent intent = new Intent(context, ShowActivity.class);
        Bundle args = ShowActivity.getArguments(event);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    static private void redirectToEDP(FragmentActivity activity, Event event) {
        List<TicketOffering> offerings = event.getTicketOfferings();
        Intent confirmIntent = new Intent(activity, OrderConfirmationActivity.class);
        confirmIntent.putExtra(OrderConfirmationActivity.EXTRA_EVENT, event);
        confirmIntent.putExtra(com.livenation.mobile.android.ticketing.analytics.AnalyticConstants.PROP_IS_SDP_SHOWN, true);
        Ticketing.showFindTicketsActivityForUrl(activity, confirmIntent, offerings.get(0).getPurchaseUrl());

    }

    static public void redirectToSDPOrEDP(final Event event, final FragmentActivity activity) {
        ConfigFileProvider provider = new ConfigFileProvider(activity);
        provider.getConfigFile(new BasicApiCallback<ConfigFileProvider.ConfigFile>() {
            @Override
            public void onResponse(ConfigFileProvider.ConfigFile response) {
                if (EventUtils.isSDPAvoidable(event, response, activity)) {
                    redirectToEDP(activity, event);
                } else {
                    redirectToSDP(activity, event);
                }
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                redirectToSDP(activity, event);
            }
        });

    }
}