package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.dialogs.CalendarDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by elodieferrais on 4/29/14.
 */
public class CalendarAdapter extends ArrayAdapter<CalendarDialogFragment.CalendarItem> {

    private static final String DATE_FORMAT = "EEE. MMM dd, yyyy";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private static final String TIME_FORMAT = "h:mm aa";
    private SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
    private LayoutInflater inflater;


    public CalendarAdapter(Context context, List<CalendarDialogFragment.CalendarItem> calendarItemList) {
        super(context, 0, calendarItemList);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_calendar_item, parent, false);
        } else {
            view = convertView;
        }

        TextView title = (TextView) view.findViewById(R.id.list_calender_item_title);

        TextView startLabel = (TextView) view.findViewById(R.id.list_calender_item_start_label);
        TextView startDate = (TextView) view.findViewById(R.id.list_calender_item_start_date);
        TextView startTime = (TextView) view.findViewById(R.id.list_calender_item_start_time);

        TextView endLabel = (TextView) view.findViewById(R.id.list_calender_item_end_label);
        TextView endDate = (TextView) view.findViewById(R.id.list_calender_item_end_date);
        TextView endTime = (TextView) view.findViewById(R.id.list_calender_item_end_time);

        View endDateWrapper = view.findViewById(R.id.list_calender_item_end_date_wrapper);

        CalendarDialogFragment.CalendarItem item = getItem(position);

        TimeZone timeZone;
        if (item.getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(item.getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }

        dateFormat.setTimeZone(timeZone);
        timeFormat.setTimeZone(timeZone);

        //Title
        title.setText(item.getName());


        //Start Date
        startLabel.setText(getLabelByDate(SELL_STATUS.START, item.getStartDate()));

        startDate.setText(dateFormat.format(item.getStartDate()));
        startTime.setText(timeFormat.format(item.getStartDate()));

        //End Date
        if (item.getEndDate() == null) {
            endDateWrapper.setVisibility(View.GONE);
        } else {
            endDateWrapper.setVisibility(View.VISIBLE);
            endLabel.setText(getLabelByDate(SELL_STATUS.END, item.getEndDate()));
            endDate.setText(dateFormat.format(item.getEndDate()));
            endTime.setText(timeFormat.format(item.getEndDate()));
        }


        return view;
    }

    private String getLabelByDate(SELL_STATUS sellStatus, Date date) {
        int dateDiff = date.compareTo(Calendar.getInstance().getTime());
        if (dateDiff > 0) {
            return getContext().getString(sellStatus.pastLabel);
        } else {
            return getContext().getString(sellStatus.futureLabel);
        }
    }

    private enum SELL_STATUS {
        START(R.string.calendar_dialog_start_date_prefix_future, R.string.calendar_dialog_start_date_prefix_past),
        END(R.string.calendar_dialog_end_date_prefix_future, R.string.calendar_dialog_end_date_prefix_past);

        int pastLabel;
        int futureLabel;

        SELL_STATUS(int pastLabel, int futureLabel) {
            this.pastLabel = pastLabel;
            this.futureLabel = futureLabel;
        }
    }
}
