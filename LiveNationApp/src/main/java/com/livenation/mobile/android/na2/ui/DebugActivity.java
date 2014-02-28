package com.livenation.mobile.android.na2.ui;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.na2.ui.support.DebugAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 2/28/14.
 */
public class DebugActivity extends ListActivity {
    private static final String ACTIONS = "com.livenation.mobile.android.na.DebugActivity.ACTIONS";
    private ArrayList<DebugAction> actions;
    private DebugActionsAdapter actionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            actions = new ArrayList<DebugAction>();
            actions.add(new DebugAction("Testytest", "123 ABC XZYW"));
        } else {
            actions = (ArrayList<DebugAction>)savedInstanceState.getSerializable(ACTIONS);
        }

        actionsAdapter = new DebugActionsAdapter(this, actions);
        setListAdapter(actionsAdapter);

        getActionBar().setTitle(R.string.debug_actionbar_title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(ACTIONS, actions);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        DebugAction action = actionsAdapter.getItem(position);
        action.doAction(this);
    }

    private class DebugActionsAdapter extends ArrayAdapter<DebugAction> {
        public DebugActionsAdapter(Context context, List<DebugAction> debugActions) {
            super(context, R.layout.list_debug_action, debugActions);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.list_debug_action, parent, false);
                view.setTag(new ViewHolder(view));
            }

            DebugAction action = getItem(position);
            ViewHolder holder = (ViewHolder)view.getTag();
            holder.actionTitle.setText(action.getName());
            holder.actionDetail.setText(action.getValue());

            return view;
        }

        private class ViewHolder {
            TextView actionTitle;
            TextView actionDetail;

            public ViewHolder(View view) {
                this.actionTitle = (TextView)view.findViewById(R.id.action_title);
                this.actionDetail = (TextView)view.findViewById(R.id.action_detail);
            }
        }
    }
}
