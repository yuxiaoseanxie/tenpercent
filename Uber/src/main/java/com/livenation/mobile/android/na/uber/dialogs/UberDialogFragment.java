package com.livenation.mobile.android.na.uber.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
    private EstimationAdapter adapter;

    public static final String EXTRA_UBER_ESTIMATES = UberDialogFragment.class.getSimpleName() + ".UBER_ESTIMATES";
    public static final String EXTRA_RESULT_ESTIMATE = UberDialogFragment.class.getSimpleName() + ".UBER_SELECTED_ESTIMATE";

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
        adapter = new EstimationAdapter(getActivity(), items);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View content = inflater.inflate(R.layout.dialog_uber_estimates, null);
        View title = inflater.inflate(R.layout.dialog_title_uber_estimates, null);
        builder.setCustomTitle(title);

        ListView listView = (ListView) content.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        builder.setView(content);

        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getTargetFragment() != null) {
            LiveNationEstimate estimate = (LiveNationEstimate) parent.getItemAtPosition(position);
            Intent data = new Intent();
            data.putExtra(EXTRA_RESULT_ESTIMATE, estimate);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
            dismiss();
        }
    }

    private class EstimationAdapter extends ArrayAdapter<LiveNationEstimate> {
        private final LayoutInflater inflater;

        public EstimationAdapter(Context context, List<LiveNationEstimate> estimates) {
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
            private final TextView time;

            public ViewHolder(View root) {
                this.title = (TextView) root.findViewById(R.id.uber_list_price_estimate_title);
                this.cost = (TextView) root.findViewById(R.id.uber_list_price_estimate_cost);
                this.capacity = (TextView) root.findViewById(R.id.uber_list_price_estimate_capacity);
                this.time = (TextView) root.findViewById(R.id.uber_list_price_estimate_capacity);
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

            public TextView getTime() {
                return time;
            }
        }
    }
}
