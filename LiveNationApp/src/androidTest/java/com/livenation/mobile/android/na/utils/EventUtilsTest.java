package com.livenation.mobile.android.na.utils;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import junit.framework.Assert;

import java.util.Calendar;
import java.util.Locale;

import android.test.InstrumentationTestCase;

/**
 * Created by elodieferrais on 3/2/15.
 */
public class EventUtilsTest extends InstrumentationTestCase{

    public void testIsAcomingEventWithNoDate() {
        Event event = new Event();
        boolean result = EventUtils.isAcomingEvent(event);
        assertFalse(result);
    }

    public void testIsAcomingEventWithDate1() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)-1);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertFalse(result);
    }

    public void testIsAcomingEventWithDate2() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK)-1);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertFalse(result);
    }

    public void testIsAcomingEventWithDate3() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR)-13);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertFalse(result);
    }

    public void testIsAcomingEventWithDate4() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-11);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertTrue(result);
    }

    public void testIsAcomingEventWithDate5() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertFalse(result);
    }

    public void testIsAcomingEventWithDate6() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK)+1);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertFalse(result);
    }

    public void testIsAcomingEventWithDate7() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR)+13);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertFalse(result);
    }

    public void testIsAcomingEventWithDate8() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)+11);
        Event event = new Event();
        event.setLocalStartTime(calendar.getTime());
        boolean result = EventUtils.isAcomingEvent(event);
        assertTrue(result);
    }
}
