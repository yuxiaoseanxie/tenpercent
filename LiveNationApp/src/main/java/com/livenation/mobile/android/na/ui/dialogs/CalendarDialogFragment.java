package com.livenation.mobile.android.na.ui.dialogs;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.ui.adapters.CalendarAdapter;
import com.livenation.mobile.android.na.utils.CalendarUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Presale;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsHelper;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.analytics.Props;
import android.mobile.livenation.com.livenationui.sso.LoginHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by elodieferrais on 4/29/14.
 */
public class CalendarDialogFragment extends LiveNationDialogFragment implements AdapterView.OnItemClickListener {
    private static final String EXTRA_EVENT = "com.livenation.mobile.android.na.ui.dialogs.CalendarDialogFragment.EVENT";
    private ListView listView;
    private CalendarAdapter adapter;
    private Event event;

    public static CalendarDialogFragment newInstance(Event event) {
        CalendarDialogFragment dialogFragment = new CalendarDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EVENT, event);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        event = getEvent();
        init(event);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_calendar_list, null);
        this.listView = (ListView) view.findViewById(R.id.calendar_dialog_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setView(view, 0, 0, 0, 0);
        return dialog;
    }

    private void init(Event event) {
        String timeZone = event.getVenue() != null ? event.getVenue().getTimeZone() : null;

        List<CalendarItem> calendarItemList = new ArrayList<CalendarItem>();
        //Add Show date item
        if (!event.getIsMegaticket()) {
            CalendarItem showDate = new CalendarItem(timeZone, getString(R.string.calendar_dialog_show_date_header_title));
            if (event.getLocalStartTime() != null) {
                showDate.setStartDate(event.getLocalStartTime());
                calendarItemList.add(showDate);
            }
        }

        //Add General onSale items
        CalendarItem generalOnSale = new CalendarItem(timeZone, getString(R.string.calendar_dialog_on_sale_general_title));
        if (event.getOnSaleDate() != null) {
            generalOnSale.setStartDate(event.getOnSaleDate());
            calendarItemList.add(generalOnSale);
        }


        //Add Presale items
        List<TicketOffering> ticketOfferingList = event.getTicketOfferings();
        for (TicketOffering ticketOffering : ticketOfferingList) {
            List<Presale> presales = ticketOffering.getPresales();
            if (presales != null) {
                for (Presale presale : presales) {
                    CalendarItem presaleItem = new CalendarItem(timeZone, presale.getName());
                    presaleItem.setStartDate(presale.getStartsAt());
                    presaleItem.setEndDate(presale.getEndsAt());
                    calendarItemList.add(presaleItem);
                }
            }
        }

        adapter = new CalendarAdapter(getActivity(), calendarItemList);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.USER_LOGGED_IN, !LoginHelper.getAuthConfiguration().isEmpty());

        LiveNationAnalytics.track(AnalyticConstants.ADD_TO_CALENDAR_TAP, AnalyticsCategory.SDP, props);
        CalendarItem item = adapter.getItem(position);
        if (item.getStartDate().compareTo(Calendar.getInstance().getTime()) > 0) {
            CalendarUtils.addEventToCalendar(item, event, getActivity());
        } else {
            Toast.makeText(getActivity(), R.string.calendar_add_event_not_possible_message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private Event getEvent() {
        Event event = (Event) getArguments().getSerializable(EXTRA_EVENT);
        return event;
    }

    public static class CalendarItem {

        private String name;
        private Date startDate;
        private Date endDate;
        private String timeZone;

        public CalendarItem(String timeZone, String name) {
            this.timeZone = timeZone;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }
    }
}
