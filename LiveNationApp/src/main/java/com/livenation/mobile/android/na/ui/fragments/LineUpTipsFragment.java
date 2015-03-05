package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.LineupTipsView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.EventTimes;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.EventTips;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TipsTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Created by elodieferrais on 2/23/15.
 */
public class LineUpTipsFragment extends Fragment {
    private static final String TIME_FORMAT = "h:mm";
    private SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
    private static final String EVENTTIPS = "com.livenation.mobile.android.na.ui.fragments.LineUpFragment.EVENTTIPS";
    private EventTips eventTips;
    private ViewGroup lineupContainer;

    public static LineUpTipsFragment newInstance(EventTips eventTips) {
        LineUpTipsFragment fragment = new LineUpTipsFragment();
        Bundle args = new Bundle();
        args.putSerializable(EVENTTIPS, eventTips);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lineup, container, false);

        final TextView header = (TextView) view.findViewById(R.id.lineup_header);
        lineupContainer = (ViewGroup) view.findViewById(R.id.lineup_container);

        header.setText(R.string.lineup_header);
        EventTips eventTips = (EventTips) getArguments().getSerializable(EVENTTIPS);
        List<ScheduledEvent> scheduledEvents = getScheduledEventSorted(eventTips);
        populateList(scheduledEvents);

        return view;
    }

    private void populateList(List<ScheduledEvent> scheduledEvents) {
        lineupContainer.removeAllViews();
        for (ScheduledEvent lineup : scheduledEvents) {
            LineupTipsView view = new LineupTipsView(getActivity());
            view.getTitle().setText(lineup.title);
            view.getTime().setText(timeFormat.format(lineup.date));
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            lineupContainer.addView(view, layoutParams);

            if (scheduledEvents.indexOf(lineup) == scheduledEvents.size()-1) {
                view.getDivider().setVisibility(View.GONE);
            } else if (lineup.type.equals(ScheduleEventType.PRE_EVENT)) {
                view.getDivider().setBackgroundDrawable(getResources().getDrawable(R.drawable.dotted_gray));
                view.getDivider().setVisibility(View.VISIBLE);
            } else {
                view.getDivider().setBackgroundColor(getResources().getColor(R.color.underscore));
                view.getDivider().setVisibility(View.VISIBLE);
            }
        }
    }

    private List<ScheduledEvent> getScheduledEventSorted(EventTips eventTips) {
        List<TipsTime> setTimes = eventTips.getSetTimes();
        EventTimes eventTimes = eventTips.getEventTimes();

        List<ScheduledEvent> scheduledEvents = new ArrayList<>();
        if (setTimes != null) {
            for (TipsTime tipsTime: setTimes) {
                scheduledEvents.add(new ScheduledEvent(tipsTime.getName(), tipsTime.getStartTimeUtc(), ScheduleEventType.EVENT));
            }
        }

        if (eventTimes.getBoxOfficeOpenTime() != null) {
            scheduledEvents.add(new ScheduledEvent(getString(R.string.tips_box_office_time), eventTimes.getBoxOfficeOpenTime(), ScheduleEventType.PRE_EVENT));
        }
        if (eventTimes.getGatesOpenTime() != null) {
            scheduledEvents.add(new ScheduledEvent(getString(R.string.tips_gate_time), eventTimes.getGatesOpenTime(), ScheduleEventType.PRE_EVENT));
        }
        if (eventTimes.getParkingOpenTime() != null) {
            scheduledEvents.add(new ScheduledEvent(getString(R.string.tips_parking_time), eventTimes.getParkingOpenTime(), ScheduleEventType.PRE_EVENT));
        }
        if (eventTimes.getStartTime() != null) {
            scheduledEvents.add(new ScheduledEvent(getString(R.string.tips_start_time), eventTimes.getStartTime(), ScheduleEventType.PRE_EVENT));
        }

        Collections.sort(scheduledEvents, new Comparator<ScheduledEvent>() {
            @Override
            public int compare(ScheduledEvent lhs, ScheduledEvent rhs) {
                return rhs.date.compareTo(lhs.date);
            }
        });

        return scheduledEvents;
    }

    private class ScheduledEvent {
        public String title;
        public Date date;
        public ScheduleEventType type;

        private ScheduledEvent(@NonNull String title, @NonNull Date date, @NonNull ScheduleEventType type) {
            this.title = title;
            this.date = date;
            this.type = type;
        }
    }
    enum  ScheduleEventType {
        PRE_EVENT,
        EVENT
    }
}
