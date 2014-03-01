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
import com.livenation.mobile.android.na2.app.Constants;
import com.livenation.mobile.android.na2.app.LiveNationApplication;
import com.livenation.mobile.android.na2.ui.support.DebugItem;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by km on 2/28/14.
 */
public class DebugActivity extends ListActivity {
    private static final String ACTIONS = "com.livenation.mobile.android.na.DebugActivity.ACTIONS";
    private ArrayList<DebugItem> actions;
    private DebugItemsAdapter actionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            actions = new ArrayList<DebugItem>();
            actions.add(new DebugItem(getString(R.string.debug_item_device_uuid), Constants.deviceId));
            AccessToken accessToken = (AccessToken)LiveNationApplication.get().getServiceApi().getAuthorizer().getAuthorization();
            actions.add(new DebugItem(getString(R.string.debug_item_access_token), accessToken.getToken()));
            actions.add(new DebugItem(getString(R.string.debug_item_apid), PushManager.shared().getAPID()));
            RichPushUser urbanAirshipUser = RichPushManager.shared().getRichPushUser();
            actions.add(new DebugItem(getString(R.string.debug_item_urban_airship_id), urbanAirshipUser.getId()));
        } else {
            actions = (ArrayList<DebugItem>)savedInstanceState.getSerializable(ACTIONS);
        }

        actionsAdapter = new DebugItemsAdapter(this, actions);
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
        DebugItem action = actionsAdapter.getItem(position);
        action.doAction(this);
    }

    private class DebugItemsAdapter extends ArrayAdapter<DebugItem> {
        public DebugItemsAdapter(Context context, List<DebugItem> debugItems) {
            super(context, R.layout.list_debug_action, debugItems);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.list_debug_action, parent, false);
                view.setTag(new ViewHolder(view));
            }

            DebugItem action = getItem(position);
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
