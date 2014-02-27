/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na2.notifications.inbox;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.livenation.mobile.android.na2.ui.HomeActivity;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.livenation.mobile.android.na2.R;
import com.urbanairship.util.UAStringUtil;

import java.util.HashSet;
import java.util.List;

/**
 * Activity that manages the activity_inbox.
 * On a tablet it also manages the message view pager.
 */
public class InboxActivity extends ActionBarActivity implements
InboxFragment.OnMessageListener,
ActionMode.Callback,
RichPushManager.Listener,
RichPushInbox.Listener {

    static final String CHECKED_IDS_KEY = "com.livenation.mobile.android.na2.notifications.CHECKED_IDS";
    static final String MESSAGE_ID_KEY = "com.livenation.mobile.android.na2.notifications.FIRST_MESSAGE_ID";
    public static final String MESSAGE_ID_RECEIVED_KEY = "com.livenation.mobile.android.na2.notifications.MESSAGE_ID_RECEIVED_KEY";

    private ActionMode actionMode;

    private ViewPager messagePager;

    private InboxFragment inbox;
    private RichPushInbox richPushInbox;
    private ActionBar actionBar;

    private List<RichPushMessage> messages;

    private Button actionSelectionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_inbox);

        actionBar = getSupportActionBar();
        configureActionBar();

        this.richPushInbox = RichPushManager.shared().getRichPushUser().getInbox();

        // Set up the activity_inbox fragment
        this.inbox = (InboxFragment) this.getSupportFragmentManager().findFragmentById(R.id.inbox);
        this.inbox.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.inbox.getListView().setBackgroundColor(Color.BLACK);

        // Set up the message view pager if it exists
        this.messagePager = (ViewPager) this.findViewById(R.id.message_pager);
        if (messagePager != null) {
            messagePager.setAdapter(new MessageFragmentAdapter(this.getSupportFragmentManager()));
            this.messagePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    messages.get(position).markRead();
                    // Highlight the current item you are viewing in the activity_inbox
                    inbox.getListView().setItemChecked(position, true);

                    // If we are in actionMode, update the menu items
                    if (actionMode != null) {
                        actionMode.invalidate();
                    }
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listen for any rich push message changes
        RichPushManager.shared().addListener(this);
        RichPushManager.shared().getRichPushUser().getInbox().addListener(this);

        // Update the rich push messages to the latest
        updateRichPushMessages();

        // Show any pending message ids from the intent
        showPendingMessageId();

        startActionModeIfNecessary();

        // Dismiss any notifications if available
        //RichNotificationBuilder.dismissInboxNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove listener for message changes
        RichPushManager.shared().removeListener(this);
        richPushInbox.removeListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    public void onMessageOpen(RichPushMessage message) {
        message.markRead();
        showMessage(message.getMessageId());

        // If we are in actionMode, update the menu items
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public void onSelectionChanged() {
        startActionModeIfNecessary();

        // If we are in actionMode, update the menu items
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inbox_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            inbox.setListShownNoAnimation(false);
            RichPushManager.shared().refreshMessages();
            break;
        }
        return true;
    }

    @Override
    @SuppressLint("NewApi")
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.inbox_actions_menu, menu);

        // Add a pop up menu to the action bar to select/deselect all
        // Pop up menu requires api >= 11
        if (Build.VERSION.SDK_INT >= 11) {
            View customView = LayoutInflater.from(this).inflate(R.layout.inbox_ab_selection_dropdown, null);
            actionSelectionButton = (Button) customView.findViewById(R.id.selection_button);

            final PopupMenu popupMenu = new PopupMenu(this, customView);
            popupMenu.getMenuInflater().inflate(R.menu.selection, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    if (item.getItemId() == R.id.menu_deselect_all) {
                        inbox.clearSelection();
                    } else {
                        inbox.selectAll();
                    }
                    return true;
                }
            });

            actionSelectionButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    android.view.Menu menu = popupMenu.getMenu();
                    menu.findItem(R.id.menu_deselect_all).setVisible(true);
                    menu.findItem(R.id.menu_select_all).setVisible( inbox.getSelectedMessages().size() != messages.size());
                    popupMenu.show();
                }

            });


            mode.setCustomView(customView);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Logger.debug("onPrepareActionMode");

        boolean selectionContainsRead = false;
        boolean selectionContainsUnread = false;

        for (String id : inbox.getSelectedMessages()) {
            RichPushMessage message = richPushInbox.getMessage(id);
            if (message.isRead()) {
                selectionContainsRead = true;
            } else {
                selectionContainsUnread = true;
            }

            if (selectionContainsRead && selectionContainsUnread) {
                break;
            }
        }

        // Show them both
        menu.findItem(R.id.mark_read).setVisible(selectionContainsUnread);
        menu.findItem(R.id.mark_unread).setVisible(selectionContainsRead);

        // If we have an action selection button update the text
        if (actionSelectionButton != null) {
            String selectionText = this.getString(R.string.cab_selection, inbox.getSelectedMessages().size());
            actionSelectionButton.setText(selectionText);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Logger.debug("onActionItemClicked");
        switch (item.getItemId()) {
        case R.id.mark_read:
            richPushInbox.markMessagesRead(new HashSet<String>(inbox.getSelectedMessages()));
            break;
        case R.id.mark_unread:
            richPushInbox.markMessagesUnread(new HashSet<String>(inbox.getSelectedMessages()));
            break;
        case R.id.delete:
            richPushInbox.deleteMessages(new HashSet<String>(inbox.getSelectedMessages()));
            break;
        default:
            return false;
        }

        actionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Logger.debug("onDestroyActionMode");
        if (actionMode != null) {
            actionMode = null;
            inbox.clearSelection();
        }
    }

    @Override
    public void onBackPressed() {
        navigateToMain();
    }

    /**
     * Navigates to the main activity and finishes the current one
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

        this.finish();
    }

    /**
     * Configures the action bar to have a navigation list of
     * 'Home' and 'Inbox'
     */
    private void configureActionBar() {
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    /**
     * Tries to show a message if the pendingMessageId is set.
     * Clears the pendingMessageId after.
     */
    private void showPendingMessageId() {
        String pendingMessageId = getIntent().
                getStringExtra(MESSAGE_ID_RECEIVED_KEY);

        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            getIntent().removeExtra(MESSAGE_ID_RECEIVED_KEY);
            showMessage(pendingMessageId);
        }
    }

    /**
     * Shows a message either in the message view pager, or by launching
     * a new MessageActivity
     * @param messageId the specified message id
     */
    private void showMessage(String messageId) {
        RichPushMessage message = richPushInbox.getMessage(messageId);

        // Message is already deleted, skip
        if (message == null) {
            return;
        }

        message.markRead();

        if (messagePager != null) {
            this.messagePager.setCurrentItem(messages.indexOf(message));
        } else {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY, messageId);
            this.startActivity(intent);
        }
    }

    /**
     * Starts the action mode if there are any selected
     * messages in the activity_inbox fragment
     */
    private void startActionModeIfNecessary() {
        List<String> checkedIds = inbox.getSelectedMessages();
        if (actionMode != null && checkedIds.isEmpty()) {
            actionMode.finish();
        } else if (actionMode == null && !checkedIds.isEmpty()) {
            actionMode = this.startSupportActionMode(this);
        }
    }

    @Override
    public void onUpdateMessages(boolean success) {
        // Stop the progress spinner and display the list
        inbox.setListShownNoAnimation(true);

        // If the message update failed
        if (!success) {
            // Show an error dialog
            DialogFragment fragment = new InboxRefreshFailedDialog();
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onUpdateUser(boolean success) {
        // no-op
    }

    @Override
    public void onUpdateInbox() {
        updateRichPushMessages();
    }

    /**
     * Grabs the latest messages from the rich push activity_inbox, and syncs them
     * with the activity_inbox fragment and message view pager if available
     */
    private void updateRichPushMessages() {
        messages = RichPushManager.shared().getRichPushUser().getInbox().getMessages();
        this.inbox.setMessages(messages);
        if (messagePager != null) {
            ((MessageFragmentAdapter) messagePager.getAdapter()).setRichPushMessages(messages);
        }
    }

    /**
     * Alert dialog for when messages fail to refresh
     */
    public static class InboxRefreshFailedDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
            .setIcon(R.drawable.icon_144)
            .setTitle(R.string.inbox_refresh_failed_dialog_title)
            .setMessage(R.string.inbox_refresh_failed_dialog_message)
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            })
            .create();
        }
    }
}
