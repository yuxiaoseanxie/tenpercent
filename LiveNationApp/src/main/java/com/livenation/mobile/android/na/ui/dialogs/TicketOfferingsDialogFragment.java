package com.livenation.mobile.android.na.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;

import java.util.List;

import io.segment.android.models.Props;

public class TicketOfferingsDialogFragment extends LivenationDialogFragment implements AdapterView.OnItemClickListener {
    private ListView listView;
    private TicketOfferingsAdapter adapter;
    private List<TicketOffering> offerings;
    private OnTicketOfferingClickedListener onTicketOfferingClickedListener;
    private Event event;

    //region Lifecycle

    public static TicketOfferingsDialogFragment newInstance(Event event) {
        TicketOfferingsDialogFragment dialogFragment = new TicketOfferingsDialogFragment();
        dialogFragment.setEvent(event);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new TicketOfferingsAdapter(getActivity(), offerings);

        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_ticket_offerings, null);
        this.listView = (ListView) view.findViewById(R.id.dialog_ticket_offerings_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setView(view, 0, 0, 0, 0);
        return dialog;
    }

    //endregion


    //region Properties

    public List<TicketOffering> getOfferings() {
        return offerings;
    }

    public void setEvent(Event event) {
        this.event = event;
        this.offerings = event.getTicketOfferings();
    }

    public OnTicketOfferingClickedListener getOnTicketOfferingClickedListener() {
        return onTicketOfferingClickedListener;
    }

    public void setOnTicketOfferingClickedListener(OnTicketOfferingClickedListener onTicketOfferingClickedListener) {
        this.onTicketOfferingClickedListener = onTicketOfferingClickedListener;
    }

    //endregion


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Props props = AnalyticsHelper.getPropsForEvent(event);
        props.put(AnalyticConstants.TYPE_OF_FIND_TICKETS_OPTIONS_SELECTED, offerings.get(position).getDisplayType());
        LiveNationAnalytics.track(AnalyticConstants.FIND_TICKETS_OPTIONS_SELECTION, AnalyticsCategory.SDP, props);

        if(getOnTicketOfferingClickedListener() != null) {
            TicketOffering offering = adapter.getItem(position);
            getOnTicketOfferingClickedListener().onTicketOfferingClicked(offering);
        }
        dismissAllowingStateLoss();
    }

    private class TicketOfferingsAdapter extends ArrayAdapter<TicketOffering> {
        public TicketOfferingsAdapter(Context context, List<TicketOffering> offerings) {
            super(context, R.layout.list_item_ticket_offering, offerings);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = (TextView) super.getView(position, convertView, parent);

            TicketOffering offering = getItem(position);
            text.setText(offering.getDisplayType());

            return text;
        }
    }


    public interface OnTicketOfferingClickedListener {
        void onTicketOfferingClicked(TicketOffering offering);
    }
}
