package com.livenation.mobile.android.na.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.ui.CashRecipientsActivity;
import com.livenation.mobile.android.na.helpers.LocationUpdateReceiver;
import com.livenation.mobile.android.na.helpers.MusicLibraryScannerHelper;
import com.livenation.mobile.android.na.notifications.NotificationsRegistrationManager;
import com.livenation.mobile.android.na.preferences.EnvironmentPreferences;
import com.livenation.mobile.android.na.ui.support.DebugItem;
import com.livenation.mobile.android.platform.api.proxy.LiveNationConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.init.Environment;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;
import com.livenation.mobile.android.platform.api.proxy.ProviderManager;
import com.livenation.mobile.android.platform.receiver.AccessTokenUpdateReceiver;
import com.livenation.mobile.android.ticketing.Ticketing;
import com.livenation.mobile.android.ticketing.testing.RecordedResponse;
import com.livenation.mobile.android.ticketing.testing.RecordingTicketService;
import com.livenation.mobile.android.ticketing.testing.TestingUtil;
import com.livenation.mobile.android.ticketing.utils.TicketingUtils;
import com.mobilitus.tm.tickets.models.Total;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by km on 2/28/14.
 */
public class DebugActivity extends LiveNationFragmentActivity implements AdapterView.OnItemClickListener, ConfigCallback, LocationUpdateReceiver.LocationUpdateListener, AccessTokenUpdateReceiver.AccessTokenUpdateListener {
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

    private ProviderManager providerManager;
    private LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver(this);
    private AccessTokenUpdateReceiver accessTokenUpdateReceiver = new AccessTokenUpdateReceiver(this);

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        providerManager = new ProviderManager();
        listView = (StickyListHeadersListView) findViewById(R.id.debug_activity_list_view);

        actions = new ArrayList<DebugItem>();
        addInfoDebugItems();
        addActionDebugItems();

        actionsAdapter = new DebugItemsAdapter(this, actions);
        listView.setAdapter(actionsAdapter);
        listView.setOnItemClickListener(this);

        getActionBar().setTitle(R.string.debug_actionbar_title);


        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER));
        LocalBroadcastManager.getInstance(this).registerReceiver(accessTokenUpdateReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.ACCESS_TOKEN_UPDATE_INTENT_FILTER));


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
        providerManager.getConfigReadyFor(this, ProviderManager.ProviderType.DEVICE_ID, ProviderManager.ProviderType.LOCATION, ProviderManager.ProviderType.ACCESS_TOKEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(accessTokenUpdateReceiver);
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
        //Environment Item
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

        //Square Cash Item
        ShowCashFlowItem showCashFlowItem = new ShowCashFlowItem(getString(R.string.cash_call_to_action));
        actions.add(showCashFlowItem);
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

    private Environment getEnvironment() {
        EnvironmentPreferences environmentPreferences = new EnvironmentPreferences(this);
        return environmentPreferences.getConfiguredEnvironment();
    }

    private void setEnvironment(Environment environment) {
        EnvironmentPreferences environmentPreferences = new EnvironmentPreferences(this);
        environmentPreferences.setConfiguredEnvironment(environment);
        accessTokenItem.setValue("...");
        actionsAdapter.notifyDataSetChanged();
        LiveNationApplication.getEnvironmentProvider().setEnvironment(environment);
        LiveNationApplication.getAccessTokenProvider().clear();
        NotificationsRegistrationManager.getInstance().register();
    }

    @Override
    public void onResponse(LiveNationConfig response) {
        if (null != deviceIdItem) {
            deviceIdItem.setValue(response.getDeviceId());
        }

        if (null != locationItem) {
            locationItem.setValue(response.getLat() + "," + response.getLng());
        }

        if (null != accessTokenItem) {
            accessTokenItem.setValue(response.getAccessToken().getToken());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != actionsAdapter) {
                    actionsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onErrorResponse(int errorCode) {
    }

    @Override
    public void onLocationUpdated(int mode, double lat, double lng) {
        if (null != locationItem) {
            locationItem.setValue(lat + "," + lng);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != actionsAdapter) {
                    actionsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onAccessTokenUpdated(AccessToken accessToken) {
        if (null != accessTokenItem) {
            accessTokenItem.setValue(accessToken.getToken());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != actionsAdapter) {
                    actionsAdapter.notifyDataSetChanged();
                }
            }
        });
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
            final String[] items = new String[Environment.values().length];
            for (int i = 0; i < items.length; i++) {
                items[i] = Environment.values()[i].toString();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(DebugActivity.this);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Environment environment = Environment.values()[i];
                    setEnvironment(environment);
                    environmentItem.setValue(environment.toString());

                    Intent mStartActivity = new Intent(DebugActivity.this, OnBoardingActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(DebugActivity.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);
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
                return getString(R.string.debug_item_commerce_qa_mode_on);
            else
                return getString(R.string.debug_item_commerce_qa_mode_off);
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
        private final String SHARED_FILE_NAME = "TicketingSession.json";

        private CommerceRecordingModeItem(String name) {
            super(name, null);
        }

        @Override
        public String getValue() {
            if (Ticketing.isSessionRecordingEnabled())
                return getString(R.string.debug_item_commerce_session_recording_on);
            else
                return getString(R.string.debug_item_commerce_session_recording_off);
        }

        @Override
        public void doAction(Context context) {
            if (Ticketing.isSessionRecordingEnabled()) {
                ArrayList<RecordedResponse> requests = RecordingTicketService.getSessionRequests();
                if (!TicketingUtils.isCollectionEmpty(requests)) {
                    try {
                        String jsonString = TestingUtil.convertRequestsToJson(requests);
                        File downloadsDirectory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                        downloadsDirectory.mkdirs();

                        File sharedFile = new File(downloadsDirectory, SHARED_FILE_NAME);
                        FileOutputStream sharedFileOutputStream = new FileOutputStream(sharedFile);
                        try {
                            sharedFileOutputStream.write(jsonString.getBytes());
                            sharedFileOutputStream.flush();
                        } finally {
                            sharedFileOutputStream.close();
                        }

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sharedFile));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Ticketing Session");
                        intent.setType("application/json");
                        startActivity(Intent.createChooser(intent, "Share Ticketing Session"));
                    } catch (IOException e) {
                        Log.e("Ticketing Session", "Could not share session", e);
                        Toast.makeText(context.getApplicationContext(), "Could not share session", Toast.LENGTH_SHORT).show();
                    }
                }

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

    private class ShowCashFlowItem extends DebugItem {
        private ShowCashFlowItem(String name) {
            super(name, null);
        }

        @Override
        public void doAction(Context context) {
            Total testTotal = new Total();
            testTotal.setGrandTotal(BigDecimal.valueOf(120.00));

            Intent intent = new Intent(DebugActivity.this, CashRecipientsActivity.class);
            intent.putExtra(CashUtils.EXTRA_TICKET_QUANTITY, 3);
            intent.putExtra(CashUtils.EXTRA_TOTAL, testTotal);
            startActivity(intent);
        }

        @Override
        public int getType() {
            return DebugItem.TYPE_ACTION;
        }
    }
}
