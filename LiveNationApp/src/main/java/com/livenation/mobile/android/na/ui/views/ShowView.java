package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShowView extends LinearLayout {
    //TODO: Move date parsing to Data Model Entity helper. This is ugly
    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(LiveNationApiService.LOCAL_START_TIME_FORMAT, Locale.US);

    private DisplayMode displayMode;

    private TextView title;
    private TextView details;
    private VerticalDate date;
    private View bottomLine;

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

        Date start;
        try {
            start = getDate(event.getLocalStartTime());
            date.setDate(start);
            details.setText(getDisplayMode().getDetails(event, start));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid start time: " + event.getLocalStartTime());
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public boolean isBottomLineVisible() {
        return (bottomLine.getVisibility() == View.VISIBLE);
    }

    public void setBottomLineVisible(Boolean bottomLineVisible) {
        if (bottomLineVisible)
            bottomLine.setVisibility(View.VISIBLE);
        else
            bottomLine.setVisibility(View.GONE);
    }

    private Date getDate(String dateRaw) throws ParseException {
        Date date = DATE_FORMATTER.parse(dateRaw);
        return date;
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.view_show, isInEditMode() ? null : this, false);

        this.title = (TextView) view.findViewById(R.id.view_show_title);
        this.details = (TextView) view.findViewById(R.id.view_show_details);
        this.date = (VerticalDate) view.findViewById(R.id.view_show_date);
        this.bottomLine = view.findViewById(R.id.view_show_bottom_line);

        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        setDisplayMode(DisplayMode.VENUE);
    }


    public static enum DisplayMode {
        VENUE {
            @Override
            String getTitle(Event event) {
                return event.getName();
            }

            @Override
            String getDetails(Event event, Date localStartTime) {
                return DateFormat.format("h:mm aa zzz", localStartTime).toString();
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