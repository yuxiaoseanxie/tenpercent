package com.livenation.mobile.android.na.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.support.DebugItem;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushUser;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by km on 2/28/14.
 */
public class DebugActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String ACTIONS = "com.livenation.mobile.android.na.DebugActivity.ACTIONS";
    private ArrayList<DebugItem> actions;

    private StickyListHeadersListView listView;
    private DebugItemsAdapter actionsAdapter;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        listView = (StickyListHeadersListView)findViewById(R.id.debug_activity_list_view);

        if(savedInstanceState == null) {
            actions = new ArrayList<DebugItem>();
            addInfoDebugItems();
            addActionDebugItems();
        } else {
            actions = (ArrayList<DebugItem>)savedInstanceState.getSerializable(ACTIONS);
        }

        actionsAdapter = new DebugItemsAdapter(this, actions);
        listView.setAdapter(actionsAdapter);
        listView.setOnItemClickListener(this);

        getActionBar().setTitle(R.string.debug_actionbar_title);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(ACTIONS, actions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.debug_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(item.getItemId() == R.id.debug_activity_menu_item_share) {
            onShareSelected();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void addInfoDebugItems()
    {
        actions.add(new DebugItem(getString(R.string.debug_item_device_uuid), Constants.deviceId));

        AccessToken accessToken = LiveNationApplication.get().getApiConfig().getAccessToken().getResult();
        String accessTokenString = accessToken != null? accessToken.getToken() : "(None)";
        actions.add(new DebugItem(getString(R.string.debug_item_access_token), accessTokenString));

        actions.add(new DebugItem(getString(R.string.debug_item_apid), PushManager.shared().getAPID()));

        RichPushUser urbanAirshipUser = RichPushManager.shared().getRichPushUser();
        String urbanAirshipUserID = urbanAirshipUser != null? urbanAirshipUser.getId() : "(None)";
        actions.add(new DebugItem(getString(R.string.debug_item_urban_airship_id), urbanAirshipUserID));
    }

    private void addActionDebugItems()
    {

    }


    public void onShareSelected()
    {
        String itemsString = DebugItem.convertListToString(actions);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Session debug info");
        shareIntent.putExtra(Intent.EXTRA_TEXT, itemsString);

        startActivity(Intent.createChooser(shareIntent, "Share info"));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        DebugItem action = actionsAdapter.getItem(position);
        action.doAction(this);
    }

    private class DebugItemsAdapter extends ArrayAdapter<DebugItem> implements StickyListHeadersAdapter {
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

        @Override
        public long getHeaderId(int position) {
            return getItem(position).getType();
        }

        @Override
        public View getHeaderView(int position, View view, ViewGroup viewGroup) {
            View headerView = view;
            if(headerView == null) {
                headerView = getLayoutInflater().inflate(R.layout.list_show_header, viewGroup, false);
                headerView.setTag(new HeaderViewHolder(headerView));
            }

            HeaderViewHolder holder = (HeaderViewHolder)headerView.getTag();

            int type = getItem(position).getType();
            switch (type) {
                case DebugItem.TYPE_INFO:
                    holder.text.setText(R.string.debug_header_info);
                    break;

                case DebugItem.TYPE_ACTION:
                    holder.text.setText(R.string.debug_header_actions);
                    break;

                default:
                    throw new RuntimeException("Invalid type given");
            }

            return headerView;
        }

        private class ViewHolder {
            TextView actionTitle;
            TextView actionDetail;

            public ViewHolder(View view) {
                this.actionTitle = (TextView)view.findViewById(R.id.action_title);
                this.actionDetail = (TextView)view.findViewById(R.id.action_detail);
            }
        }

        private class HeaderViewHolder {
            TextView text;

            public HeaderViewHolder(View view) {
                this.text = (TextView)view.findViewById(R.id.list_show_header_textview);
            }
        }
    }
}
