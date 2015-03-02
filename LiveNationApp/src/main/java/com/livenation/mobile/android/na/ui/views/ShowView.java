package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowView extends LinearLayout {
    private DisplayMode displayMode;

    private TextView title;
    private TextView details;
    private VerticalDate date;
    private View megaticket;

    public ShowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShowView(Context context) {
        super(context);
        init(context);
    }

    public void setEvent(Event event) {
        title.setText(ShowViewController.getTitle(getDisplayMode(), event));

        Date start = event.getLocalStartTime();
        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        date.setDate(start, timeZone, event.getIsMegaticket());
        details.setText(ShowViewController.getDetails(getDisplayMode(), event, start));

        if (event.getIsMegaticket()) {
            megaticket.setVisibility(View.VISIBLE);
        } else {
            megaticket.setVisibility(View.GONE);
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.view_show, isInEditMode() ? null : this, false);

        this.title = (TextView) view.findViewById(R.id.view_show_title);
        this.details = (TextView) view.findViewById(R.id.view_show_details);
        this.date = (VerticalDate) view.findViewById(R.id.view_show_date);
        this.megaticket = view.findViewById(R.id.view_show_megaticket);

        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        setDisplayMode(DisplayMode.VENUE);
    }


    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("h:mm aa zzz", Locale.getDefault());

    private static class ShowViewController {
        static String getTitle(DisplayMode displayMode, Event event) {
            switch (displayMode) {
                case VENUE:
                    return event.getName();
                case EVENT:
                    return event.getVenue().getName();
                case ARTIST:
                    return event.getVenue().getName();
                default:
                    return null;
            }

        }

        static String getDetails(DisplayMode displayMode, Event event, Date localStartTime) {
            switch (displayMode) {
                case VENUE:
                    TimeZone timeZone;
                    if (event.getVenue().getTimeZone() != null) {
                        timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
                    } else {
                        timeZone = TimeZone.getDefault();
                    }
                    TIME_FORMATTER.setTimeZone(timeZone);
                    return TIME_FORMATTER.format(localStartTime);
                case EVENT:
                    return event.getVenue().getName();
                case ARTIST:
                    Venue venue = event.getVenue();
                    return String.format("%s, %s", venue.getAddress().getCity(), venue.getAddress().getState());
                default:
                    return null;
            }
        }
    }

    public static enum DisplayMode {
        VENUE,
        ARTIST,
        EVENT
    }
}