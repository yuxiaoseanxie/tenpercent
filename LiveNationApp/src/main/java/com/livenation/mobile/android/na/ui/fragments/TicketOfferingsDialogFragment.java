package com.livenation.mobile.android.na.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;

import java.util.List;

public class TicketOfferingsDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private TicketOfferingsAdapter adapter;
    private List<TicketOffering> offerings;
    private OnTicketOfferingClickedListener onTicketOfferingClickedListener;

    //region Lifecycle

    public static TicketOfferingsDialogFragment newInstance(List<TicketOffering> offerings) {
        TicketOfferingsDialogFragment dialogFragment = new TicketOfferingsDialogFragment();
        dialogFragment.setOfferings(offerings);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.ticket_offerings);
        builder.setAdapter(adapter, this);
        return builder.create();
    }

    //endregion


    //region Properties

    public List<TicketOffering> getOfferings() {
        return offerings;
    }

    public void setOfferings(List<TicketOffering> offerings) {
        this.offerings = offerings;
    }

    public OnTicketOfferingClickedListener getOnTicketOfferingClickedListener() {
        return onTicketOfferingClickedListener;
    }

    public void setOnTicketOfferingClickedListener(OnTicketOfferingClickedListener onTicketOfferingClickedListener) {
        this.onTicketOfferingClickedListener = onTicketOfferingClickedListener;
    }

    //endregion


    @Override
    public void onClick(DialogInterface dialogInterface, int position) {
        if(getOnTicketOfferingClickedListener() != null) {
            TicketOffering offering = adapter.getItem(position);
            getOnTicketOfferingClickedListener().ticketOfferingClicked(offering);
        }
        dismissAllowingStateLoss();
    }

    private class TicketOfferingsAdapter extends ArrayAdapter<TicketOffering> {
        public TicketOfferingsAdapter(Context context, List<TicketOffering> offerings) {
            super(context, android.R.layout.simple_expandable_list_item_1, offerings);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView text = (TextView) super.getView(position, convertView, parent);

            TicketOffering offering = getItem(position);
            text.setText(offering.getName());

            return text;
        }
    }


    public interface OnTicketOfferingClickedListener {
        void ticketOfferingClicked(TicketOffering offering);
    }
}
