package com.livenation.mobile.android.na.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ListView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.adapters.CalendarStickyListHeaderAdapter;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Presale;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by elodieferrais on 4/29/14.
 */
public class CalendarDialogFragment extends DialogFragment {
    private StickyListHeadersListView listView;
    private CalendarStickyListHeaderAdapter adapter;
    private Event event;

    public static CalendarDialogFragment newInstance(Event event) {
        CalendarDialogFragment dialogFragment = new CalendarDialogFragment();
        dialogFragment.setEvent(event);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(event);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_calendar_list, null);
        this.listView = (StickyListHeadersListView) view.findViewById(R.id.dialog_ticket_offerings_list);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setView(view, 0, 0, 0, 0);
        return dialog;
    }

    private void setEvent(Event event) {
        this.event = event;
    }

    private void init(Event event) {
        List<CalendarItem> calendarItemList = new ArrayList<CalendarItem>();
        //Add Show date item
        CalendarItem showDate = new CalendarItem("Show Date");
        showDate.setStartDate(event.getLocalStartTime());
        calendarItemList.add(showDate);

        //Add Genaral onSale items
        List<TicketOffering> ticketOfferingList = event.getTicketOfferings();
        for (int i = 0; i < ticketOfferingList.size(); i++) {
            TicketOffering ticketOffering = ticketOfferingList.get(i);
            CalendarItem generalOnSale = new CalendarItem(ticketOffering.getName());
            generalOnSale.setStartDate(ticketOffering.getOnSaleAt());
            calendarItemList.add(generalOnSale);

            //Add presale items
            List<Presale> presales = ticketOffering.getPresales();
            if (presales != null) {
                for (Presale presale : presales) {
                    CalendarItem presaleItem = new CalendarItem(presale.getName());
                    presaleItem.setStartDate(presale.getStartsAt());
                    presaleItem.setEndDate(presale.getEndsAt());
                    calendarItemList.add(presaleItem);
                }
            }
        }

        adapter = new CalendarStickyListHeaderAdapter(getActivity(), calendarItemList);
    }

    public static class CalendarItem {

        private String name;
        private Date startDate;
        private Date endDate;

        public CalendarItem(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
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
    }
}
