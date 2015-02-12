/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na.notifications.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.ui.FavoriteActivity;
import com.urbanairship.richpush.RichPushMessage;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * A list fragment that shows rich push messages.
 */
public abstract class BaseInboxFragment extends ListFragment implements View.OnClickListener {
    private OnMessageListener listener;
    private RichPushMessageAdapter adapter;
    private List<String> selectedMessageIds = new ArrayList<String>();
    private List<RichPushMessage> messages;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.setActivityAsListener(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the RichPushMessageAdapter
        this.adapter = new RichPushMessageAdapter(getActivity(), getRowLayoutId());
        adapter.setViewBinder(createMessageBinder());
        this.setListAdapter(adapter);

        // Retain the instance so we keep list position and selection on activity re-creation
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.fragment_inbox_list_empty_view, getListView(), false);
        ((ViewGroup) getListView().getParent()).addView(view);

        LinearLayout favoriteButton = (LinearLayout) view.findViewById(R.id.notif_no_notification_favorite_button);
        favoriteButton.setOnClickListener(this);

        getListView().setEmptyView(view);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        Props props = new Props();
        RichPushMessage message = adapter.getItem(position);
        props.put(AnalyticConstants.NOTIFICATION_NAME, message.getTitle());
        props.put(AnalyticConstants.NOTIFICATION_ID, message.getMessageId());
        LiveNationAnalytics.track(AnalyticConstants.NOTIFICATION_CELL_TAP, AnalyticsCategory.NOTIFICATION, props);
        this.listener.onMessageOpen(this.adapter.getItem(position));
    }

    @Override
    public void onClick(View v) {
        LiveNationAnalytics.track(AnalyticConstants.FAVORITES_UPSELL_TAP, AnalyticsCategory.NOTIFICATION);
        Intent favoriteIntent = new Intent(v.getContext(), FavoriteActivity.class);
        startActivity(favoriteIntent);
    }

    /**
     * Sets the rich push messages to display
     *
     * @param messages Current list of rich push messages
     */
    public void setMessages(List<RichPushMessage> messages) {
        this.messages = messages;
        adapter.setMessages(messages);
    }

    /**
     * @return The list of ids of the selected messages
     */
    public List<String> getSelectedMessages() {
        return selectedMessageIds;
    }

    /**
     * Clears the selected messages
     */
    public void clearSelection() {
        selectedMessageIds.clear();
        adapter.notifyDataSetChanged();
        listener.onSelectionChanged();
    }

    /**
     * Selects all the messages in the activity_inbox
     */
    public void selectAll() {
        selectedMessageIds.clear();
        for (RichPushMessage message : messages) {
            selectedMessageIds.add(message.getMessageId());
        }
        adapter.notifyDataSetChanged();
        listener.onSelectionChanged();
    }

    /**
     * @return The layout id to use in the RichPushMessageAdapter
     */
    public abstract int getRowLayoutId();

    /**
     * @return The string id of the activity_message to display when no messages are available
     */
    public abstract int getEmptyListStringId();

    /**
     * Tries to set the activity as an OnMessageListener
     *
     * @param activity The specified activity
     */
    private void setActivityAsListener(Activity activity) {
        try {
            this.listener = (OnMessageListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activities using BaseInboxFragment must implement " +
                    "the BaseInboxFragment.OnMessageListener interface.");
        }
    }

    /**
     * Sets a activity_message is selected or not
     *
     * @param messageId The id of the activity_message
     * @param isChecked Boolean indicating if the activity_message is selected or not
     */
    protected void onMessageSelected(String messageId, boolean isChecked) {
        if (isChecked && !selectedMessageIds.contains(messageId)) {
            selectedMessageIds.add(messageId);
        } else if (!isChecked && selectedMessageIds.contains(messageId)) {
            selectedMessageIds.remove(messageId);
        }

        listener.onSelectionChanged();
    }

    /**
     * Returns if a activity_message is selected
     *
     * @param messageId The id of the activity_message
     * @return <code>true</code> If the activity_message is selected, <code>false</code> otherwise.
     */
    protected boolean isMessageSelected(String messageId) {
        return selectedMessageIds.contains(messageId);
    }

    /**
     * @return RichPushMessageAdapter.ViewBinder to bind messages to a list view item
     * in the list adapter.
     */
    protected abstract RichPushMessageAdapter.ViewBinder createMessageBinder();

    /**
     * Listens for activity_message selection and selection changes
     */
    public interface OnMessageListener {
        void onMessageOpen(RichPushMessage message);

        void onSelectionChanged();
    }


}
