package com.livenation.mobile.android.na.utils;

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.CalendarDialogFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by elodieferrais on 5/1/14.
 */
public class CalendarUtils {

    public static void addEventToCalendar(CalendarDialogFragment.CalendarItem calendarItem, Event event, Activity activity) {
        Date endDate = calendarItem.getEndDate();
        long endDateInMilliSec;
        if (endDate == null) {
            Calendar endDateCal = Calendar.getInstance();
            endDateCal.setTimeInMillis(calendarItem.getStartDate().getTime());
            endDateCal.add(Calendar.HOUR, 2);
            endDateInMilliSec = endDateCal.getTimeInMillis();
        } else {
            endDateInMilliSec = endDate.getTime();
        }

        Intent intent = new Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendarItem.getStartDate().getTime())
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDateInMilliSec)
        .putExtra(CalendarContract.Events.TITLE, event.getDisplayName() + " - " + calendarItem.getName())
        .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getVenue().getName())
        .putExtra(CalendarContract.Events.DESCRIPTION, activity.getApplicationContext().getString(R.string.calendar_event_description_url_base) + event.getId());
        activity.startActivity(intent);
    }
}
