package com.livenation.mobile.android.na.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.OrderConfirmationActivity;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;
import com.livenation.mobile.android.ticketing.Ticketing;

import java.util.Calendar;
import java.util.List;

/**
 * Created by elodieferrais on 10/20/14.
 */
public class EventUtils {
    static public boolean isSDPAvoidable(Event event, Context context) {
        List<TicketOffering> offerings = event.getTicketOfferings();
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SharedPreferences.AB_TESTING, Context.MODE_PRIVATE);
        boolean isNewUser = sharedPreferences.getBoolean(Constants.SharedPreferences.IS_NEW_USER, true);

        return (event.getOnSaleDate()!= null
                && event.getOnSaleDate().compareTo(Calendar.getInstance().getTime()) <= 0
                && offerings.size() == 1
                && Ticketing.isTicketmasterUrl(offerings.get(0).getPurchaseUrl())
                && LiveNationApplication.get().getInstalledAppConfig().isCommerceAvailable()
                && isNewUser
                );
    }

    static public void redirectToSDPOrEDP(Event event, final FragmentActivity activity) {
        Intent intent = new Intent(activity, ShowActivity.class);
        List<TicketOffering> offerings = event.getTicketOfferings();
        if (EventUtils.isSDPAvoidable(event, activity)) {
            Intent confirmIntent = new Intent(activity, OrderConfirmationActivity.class);
            confirmIntent.putExtra(OrderConfirmationActivity.EXTRA_EVENT, event);
            confirmIntent.putExtra(com.livenation.mobile.android.ticketing.analytics.AnalyticConstants.PROP_IS_SDP_SHOWN, true);
            Ticketing.showFindTicketsActivityForUrl(activity, confirmIntent, offerings.get(0).getPurchaseUrl());

        } else {
            Bundle args = ShowActivity.getArguments(event);
            intent.putExtras(args);
            activity.startActivity(intent);
        }
    }
}
