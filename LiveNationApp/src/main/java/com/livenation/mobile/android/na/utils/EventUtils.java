package com.livenation.mobile.android.na.utils;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;
import com.livenation.mobile.android.ticketing.Ticketing;

import java.util.Calendar;
import java.util.List;

/**
 * Created by elodieferrais on 10/20/14.
 */
public class EventUtils {
    static public boolean isSDPAvoidable(Event event) {
        List<TicketOffering> offerings = event.getTicketOfferings();
        return (event.getOnSaleDate()!= null
                && event.getOnSaleDate().compareTo(Calendar.getInstance().getTime()) <= 0
                && offerings.size() == 1
                && Ticketing.isTicketmasterUrl(offerings.get(0).getPurchaseUrl())
                && LiveNationApplication.get().getInstalledAppConfig().isCommerceAvailable());
    }
}
