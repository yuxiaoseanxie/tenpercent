package com.livenation.mobile.android.na.uber.dialogs;

import android.annotation.SuppressLint;
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

import com.livenation.mobile.android.na.uber.R;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 11/18/14.
 */
public class UberDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private PriceCapacityAdapter adapter;

    public static final String EXTRA_UBER_ESTIMATES = UberDialogFragment.class.getSimpleName() + ".UBER_ESTIMATES";


    public static UberDialogFragment newInstance(ArrayList<LiveNationEstimate> estimates) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_UBER_ESTIMATES, estimates);
        UberDialogFragment dialog = new UberDialogFragment();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<LiveNationEstimate> items = (ArrayList<LiveNationEstimate>) getArguments().getSerializable(EXTRA_UBER_ESTIMATES);
        adapter = new PriceCapacityAdapter(getActivity(), items);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View titleView = inflater.inflate(R.layout.dialog_uber_estimates, null);

        ListView listView = (ListView) titleView.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        builder.setView(titleView);

        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //getTargetFragment().onActivityResult();
    }

    private class PriceCapacityAdapter extends ArrayAdapter<LiveNationEstimate> {
        private final LayoutInflater inflater;

        public PriceCapacityAdapter(Context context, List<LiveNationEstimate> estimates) {
            super(context, android.R.layout.simple_list_item_1);
            inflater = LayoutInflater.from(context);
            addAll(estimates);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_row_uber_estimate, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LiveNationEstimate estimate = getItem(position);
            holder.getTitle().setText(estimate.getPrice().getDisplayName());

            if (estimate.hasProduct()) {
                int count = estimate.getProduct().getCapacity();
                holder.getCapacity().setText(getResources().getQuantityString(R.plurals.uber_capacity, count, count));
            }

            holder.getCost().setText(estimate.getPrice().getEstimate());
            return convertView;
        }

        private class ViewHolder {
            private final TextView title;
            private final TextView cost;
            private final TextView capacity;

            public ViewHolder(View root) {
                this.title = (TextView) root.findViewById(R.id.uber_list_price_estimate_title);
                this.cost = (TextView) root.findViewById(R.id.uber_list_price_estimate_capacity);
                this.capacity = (TextView) root.findViewById(R.id.uber_list_price_estimate_cost);
            }

            public TextView getCost() {
                return cost;
            }

            public TextView getTitle() {
                return title;
            }

            public TextView getCapacity() {
                return capacity;
            }
        }
    }
}
