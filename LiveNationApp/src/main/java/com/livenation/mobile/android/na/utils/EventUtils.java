package com.livenation.mobile.android.na.utils;


import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by elodieferrais on 10/20/14.
 */
public class EventUtils {

    static public void redirectToSDP(Context context, Event event) {
        Intent intent = new Intent(context, ShowActivity.class);
        Bundle args = ShowActivity.getArguments(event);
        intent.putExtras(args);
        context.startActivity(intent);
    }

    static public void redirectToSDP(Context context, String eId) {
        String eventId = Event.makeTypedId(eId);
        Intent intent = new Intent(context, ShowActivity.class);
        intent.putExtras(ShowActivity.getArguments(eventId));
        context.startActivity(intent);
    }

    static public boolean isAcomingEvent(Event event) {
        Date startDate = event.getLocalStartTime();
        if (startDate != null) {
            long timeDiff = startDate.getTime() - Calendar.getInstance(Locale.getDefault()).getTimeInMillis();
            if (Math.abs(timeDiff) <= (12 * 60 * 60 * 1000)) {
                return true;
            }
        }

        return false;
    }
}