package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.CalendarDialogFragment;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by elodieferrais on 4/29/14.
 */
public class CalendarStickyListHeaderAdapter extends ArrayAdapter<CalendarDialogFragment.CalendarItem> implements StickyListHeadersAdapter {

    private LayoutInflater inflater;

    public CalendarStickyListHeaderAdapter(Context context, List<CalendarDialogFragment.CalendarItem> calendarItemList) {
        super(context, 0, calendarItemList);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getHeaderView(int pos, View convertView, ViewGroup viewGroup) {
        View view;
        if (null == convertView) {
            view = inflater.inflate(R.layout.list_calendar_header, null);
        } else {
            view = convertView;
        }

        TextView title = (TextView) view.findViewById(R.id.list_calender_header_title);

        title.setText(getItem(pos).getName());

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_calendar_item, parent, false);
        } else {
            view = convertView;
        }

        TextView startType = (TextView) view.findViewById(R.id.list_calender_item_start_type);
        TextView startDate = (TextView) view.findViewById(R.id.list_calender_item_start_date);
        TextView startTime = (TextView) view.findViewById(R.id.list_calender_item_start_time);

        TextView endType = (TextView) view.findViewById(R.id.list_calender_item_end_type);
        TextView endDate = (TextView) view.findViewById(R.id.list_calender_item_end_date);
        TextView endTime = (TextView) view.findViewById(R.id.list_calender_item_end_time);

        View endDateWrapper = view.findViewById(R.id.list_calender_item_end_date_wrapper);

        CalendarDialogFragment.CalendarItem item = getItem(position);
        startDate.setText("ST");
        startDate.setText(item.getStartDate().toString());
        startTime.setText("blabla");

        if (item.getEndDate() == null) {
            endDateWrapper.setVisibility(View.GONE);
        } else {
            endDateWrapper.setVisibility(View.VISIBLE);
            endDate.setText("END");
            endDate.setText(item.getEndDate().toString());
            endTime.setText("blabla");
        }


        return view;
    }
}
