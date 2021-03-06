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
        title.setText(getDisplayMode().getTitle(event));

        Date start = event.getLocalStartTime();
        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        date.setDate(start, timeZone, event.getIsMegaticket());
        details.setText(getDisplayMode().getDetails(event, start));

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


    public static enum DisplayMode {
        VENUE {
            private final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("h:mm aa zzz", Locale.getDefault());

            @Override
            String getTitle(Event event) {
                return event.getName();
            }

            @Override
            String getDetails(Event event, Date localStartTime) {
                TimeZone timeZone;
                if (event.getVenue().getTimeZone() != null) {
                    timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
                } else {
                    timeZone = TimeZone.getDefault();
                }
                TIME_FORMATTER.setTimeZone(timeZone);
                return TIME_FORMATTER.format(localStartTime);
            }
        },
        ARTIST {
            @Override
            String getTitle(Event event) {
                return event.getVenue().getName();
            }

            @Override
            String getDetails(Event event, Date localStartTime) {
                Venue venue = event.getVenue();
                return String.format("%s, %s", venue.getAddress().getCity(), venue.getAddress().getState());
            }
        },
        EVENT {
            @Override
            String getTitle(Event event) {
                return event.getDisplayName();
            }

            @Override
            String getDetails(Event event, Date localStartTime) {
                return event.getVenue().getName();
            }
        };

        abstract String getTitle(Event event);

        abstract String getDetails(Event event, Date localStartTime);
    }
}