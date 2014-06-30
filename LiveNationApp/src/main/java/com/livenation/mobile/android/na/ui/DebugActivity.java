package com.livenation.mobile.android.na.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.apiconfig.ConfigManager;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.MusicLibraryScannerHelper;
import com.livenation.mobile.android.na.notifications.NotificationsRegistrationManager;
import com.livenation.mobile.android.na.ui.support.DebugItem;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.ticketing.Ticketing;
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
public class DebugActivity extends LiveNationFragmentActivity implements AdapterView.OnItemClickListener, ApiServiceBinder {
    private static final String ACTIONS = "com.livenation.mobile.android.na.DebugActivity.ACTIONS";
    private ArrayList<DebugItem> actions;
    private StickyListHeadersListView listView;
    private DebugItemsAdapter actionsAdapter;
    private DebugItem deviceIdItem;
    private DebugItem accessTokenItem;
    private DebugItem environmentItem;
    private DebugItem locationItem;
    private DebugItem scanItem;
    private DebugItem versionName;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        listView = (StickyListHeadersListView) findViewById(R.id.debug_activity_list_view);

        actions = new ArrayList<DebugItem>();
        addInfoDebugItems();
        addActionDebugItems();

        actionsAdapter = new DebugItemsAdapter(this, actions);
        listView.setAdapter(actionsAdapter);
        listView.setOnItemClickListener(this);

        getActionBar().setTitle(R.string.debug_actionbar_title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.debug_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.debug_activity_menu_item_share) {
            onShareSelected();
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LiveNationApplication.get().getConfigManager().persistentBindApi(DebugActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LiveNationApplication.get().getConfigManager().persistentUnbindApi(DebugActivity.this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        if (null != accessTokenItem) {
            accessTokenItem.setValue(apiService.getApiConfig().getAccessToken());
        }
        if (null != deviceIdItem) {
            deviceIdItem.setValue(apiService.getApiConfig().getDeviceId());
        }
        if (null != locationItem) {
            locationItem.setValue(apiService.getApiConfig().getLat() + "," + apiService.getApiConfig().getLng());
        }
        if (null != actionsAdapter) {
            actionsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onApiServiceNotAvailable() {

    }

    private void addInfoDebugItems() {
        deviceIdItem = new DebugItem(getString(R.string.debug_item_device_uuid), "...");

        actions.add(deviceIdItem);

        accessTokenItem = new DebugItem(getString(R.string.debug_item_access_token), "...");
        actions.add(accessTokenItem);

        actions.add(new DebugItem(getString(R.string.debug_item_apid), PushManager.shared().getAPID()));

        RichPushUser urbanAirshipUser = RichPushManager.shared().getRichPushUser();
        String urbanAirshipUserID = urbanAirshipUser != null ? urbanAirshipUser.getId() : "(None)";
        actions.add(new DebugItem(getString(R.string.debug_item_urban_airship_id), urbanAirshipUserID));

        locationItem = new DebugItem("Location", "");
        actions.add(locationItem);

        versionName = new DebugItem("Version", BuildConfig.VERSION_NAME);
        actions.add(versionName);

        DebugItem gitSha = new DebugItem("SHA-Application", BuildConfig.GIT_SHA_LIVENATIONAPP);
        actions.add(gitSha);

        gitSha = new DebugItem("SHA-Platform", BuildConfig.GIT_SHA_LABSPLATFORM);
        actions.add(gitSha);

        gitSha = new DebugItem("SHA-Ticketing", BuildConfig.GIT_SHA_TICKETING);
        actions.add(gitSha);
    }

    private void addActionDebugItems() {
        //Environnement Item
        environmentItem = new HostDebugItem(getString(R.string.debug_item_environment), getEnvironment().toString());
        actions.add(environmentItem);

        //Scan Item
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SharedPreferences.DEBUG_MODE_DATA, MODE_PRIVATE);
        Boolean isDebugModeActivited = sharedPreferences.getBoolean(Constants.SharedPreferences.DEBUG_MODE_IS_DEBUG_MODE_ACTIVATED, false);
        String scanValue = ScanOptions.HIDE_TOAST.message;
        if (isDebugModeActivited) {
            scanValue = ScanOptions.SHOW_TOAST.message;
        }
        scanItem = new ScanItem(getString(R.string.debug_item_scan, MusicLibraryScannerHelper.artistNumber), scanValue);
        actions.add(scanItem);

        //Commerce QA Mode Item
        CommerceQAModeItem commerceQAModeItem = new CommerceQAModeItem(getString(R.string.debug_item_commerce_qa_mode));
        actions.add(commerceQAModeItem);

        //Commerce session recording item
        CommerceRecordingModeItem commerceRecordingModeItem = new CommerceRecordingModeItem(getString(R.string.debug_item_commerce_session_recording));
        actions.add(commerceRecordingModeItem);
    }

    public void onShareSelected() {
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

    private Constants.Environment getEnvironment() {
        return ConfigManager.getConfiguredEnvironment(this);
    }

    private void setEnvironment(Constants.Environment environment) {
        ConfigManager.setConfiguredEnvironment(environment, this);
        accessTokenItem.setValue("...");
        actionsAdapter.notifyDataSetChanged();
        ConfigManager configManager = LiveNationApplication.get().getConfigManager();
        configManager.clearAccessToken();
        configManager.buildApi();
        NotificationsRegistrationManager.getInstance().register();
    }

    private static enum ScanOptions {
        HIDE_TOAST("Hide toast"),
        SHOW_TOAST("Show toast");
        private String message;

        ScanOptions(String message) {
            this.message = message;
        }
    }

    private class DebugItemsAdapter extends ArrayAdapter<DebugItem> implements StickyListHeadersAdapter {
        public DebugItemsAdapter(Context context, List<DebugItem> debugItems) {
            super(context, R.layout.list_debug_action, debugItems);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_debug_action, parent, false);
                view.setTag(new ViewHolder(view));
            }

            DebugItem action = getItem(position);
            ViewHolder holder = (ViewHolder) view.getTag();
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
            if (headerView == null) {
                headerView = getLayoutInflater().inflate(R.layout.list_show_header, viewGroup, false);
                headerView.setTag(new HeaderViewHolder(headerView));
            }

            HeaderViewHolder holder = (HeaderViewHolder) headerView.getTag();

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
                this.actionTitle = (TextView) view.findViewById(R.id.action_title);
                this.actionDetail = (TextView) view.findViewById(R.id.action_detail);
            }
        }

        private class HeaderViewHolder {
            TextView text;

            public HeaderViewHolder(View view) {
                this.text = (TextView) view.findViewById(R.id.list_show_header_textview);
            }
        }
    }

    private class HostDebugItem extends DebugItem {
        private HostDebugItem(String name, String value) {
            super(name, value);
        }

        @Override
        public void doAction(Context context) {
            final String[] items = new String[Constants.Environment.values().length];
            for (int i = 0; i < items.length; i++) {
                items[i] = Constants.Environment.values()[i].toString();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(DebugActivity.this);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Constants.Environment environment = Constants.Environment.values()[i];
                    setEnvironment(environment);
                    environmentItem.setValue(environment.toString());
                    actionsAdapter.notifyDataSetChanged();
                }
            });

            builder.create().show();

        }

        @Override
        public int getType() {
            return DebugItem.TYPE_ACTION;
        }
    }

    private class ScanItem extends DebugItem {
        private ScanItem(String name, String value) {
            super(name, value);
        }

        @Override
        public void doAction(final Context context) {
            String[] items = new String[ScanOptions.values().length];
            for (int i = 0; i < ScanOptions.values().length; i++) {
                items[i] = ScanOptions.values()[i].message;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(DebugActivity.this);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ScanOptions scanOption = ScanOptions.values()[i];
                    SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SharedPreferences.DEBUG_MODE_DATA, MODE_PRIVATE).edit();
                    switch (scanOption) {
                        case SHOW_TOAST:
                            editor.putBoolean(Constants.SharedPreferences.DEBUG_MODE_IS_DEBUG_MODE_ACTIVATED, true);
                            break;
                        case HIDE_TOAST:
                            editor.putBoolean(Constants.SharedPreferences.DEBUG_MODE_IS_DEBUG_MODE_ACTIVATED, false);
                            break;

                    }
                    scanItem.setValue(scanOption.message);
                    actionsAdapter.notifyDataSetChanged();
                    editor.commit();
                }
            });

            builder.create().show();

        }

        @Override
        public int getType() {
            return DebugItem.TYPE_ACTION;
        }
    }

    private class CommerceQAModeItem extends DebugItem {
        private CommerceQAModeItem(String name) {
            super(name, null);
        }

        @Override
        public String getValue() {
            if (Ticketing.isQaModeEnabled())
                return getString(R.string.debug_item_mode_on);
            else
                return getString(R.string.debug_item_mode_off);
        }

        @Override
        public void doAction(Context context) {
            if (Ticketing.isQaModeEnabled()) {
                Ticketing.setQaModeEnabled(false);
            } else {
                Ticketing.setQaModeEnabled(true);
            }

            actionsAdapter.notifyDataSetChanged();
        }

        @Override
        public int getType() {
            return DebugItem.TYPE_ACTION;
        }
    }

    private class CommerceRecordingModeItem extends DebugItem {
        private CommerceRecordingModeItem(String name) {
            super(name, null);
        }

        @Override
        public String getValue() {
            if (Ticketing.isSessionRecordingEnabled())
                return getString(R.string.debug_item_mode_on);
            else
                return getString(R.string.debug_item_mode_off);
        }

        @Override
        public void doAction(Context context) {
            if (Ticketing.isSessionRecordingEnabled()) {
                // TODO: Share session recording here.
                Ticketing.setSessionRecordingEnabled(false);
            } else {
                Ticketing.setSessionRecordingEnabled(true);
            }

            actionsAdapter.notifyDataSetChanged();
        }

        @Override
        public int getType() {
            return DebugItem.TYPE_ACTION;
        }
    }
}
