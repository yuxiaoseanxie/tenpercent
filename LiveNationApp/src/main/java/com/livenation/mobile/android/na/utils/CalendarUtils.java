package com.livenation.mobile.android.na.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.CalendarContract;
import android.widget.Toast;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.dialogs.CalendarDialogFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendarItem.getStartDate().getTime())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDateInMilliSec)
                .putExtra(CalendarContract.Events.TITLE, event.getDisplayName() + " - " + calendarItem.getName())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getVenue().getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, activity.getApplicationContext().getString(R.string.calendar_event_description_url_base) + event.getId());

        launchCalendar(intent, activity);
    }

    public static void addEventToCalendar(CalendarDialogFragment.CalendarItem calendarItem, String evendId, Activity activity) {
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

        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendarItem.getStartDate().getTime())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDateInMilliSec)
                .putExtra(CalendarContract.Events.TITLE, calendarItem.getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, activity.getApplicationContext().getString(R.string.calendar_event_description_url_base) + evendId);

        launchCalendar(intent, activity);
    }

    private static void launchCalendar(Intent intent, Activity activity) {
        PackageManager manager = activity.getPackageManager();
        List<ResolveInfo> resolveInfos = manager.queryIntentActivities(intent, 0);
        if (resolveInfos.size() > 0) {
            activity.startActivity(intent);
        }else{
            Toast.makeText(activity, R.string.calendar_add_event_not_supported_message, Toast.LENGTH_SHORT).show();
        }

    }
}
