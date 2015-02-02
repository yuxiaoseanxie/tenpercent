/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.livenation.mobile.android.na.notifications.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.notifications.UrbanAirshipRequest;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Fragment that displays a rich push activity_message in a RichPushMessageView
 */
public class MessageFragment extends Fragment {
    private static final SimpleDateFormat DATE_RECEIVED_FORMAT = new SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault());

    private RichPushMessage message;
    private TextView messageView;
    private TextView subjectText;
    private TextView dateReceivedText;
    private Button callToActionButton;
    private View errorView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container);

        messageView = (TextView) view.findViewById(R.id.fragment_message_view);
        subjectText = (TextView) view.findViewById(R.id.fragment_message_subject);
        dateReceivedText = (TextView) view.findViewById(R.id.fragment_message_date);
        callToActionButton = (Button) view.findViewById(R.id.fragment_message_cta_button);
        callToActionButton.setOnClickListener(new CallToActionClickListener());
        errorView = view.findViewById(R.id.fragment_message_error_view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String messageId = getActivity().getIntent().getStringExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY);
        message = UAirship.shared().getRichPushManager().getRichPushInbox().getMessage(messageId);
        if (message != null) {
            message.markRead();
            subjectText.setText(message.getTitle());
            dateReceivedText.setText(DATE_RECEIVED_FORMAT.format(message.getSentDate()));
            loadAndCustomizeMessage();

            Bundle messageExtras = message.getExtras();
            if (messageExtras.containsKey(Constants.Notifications.EXTRA_MESSAGE_ACTION_URL)) {
                String title = messageExtras.getString(Constants.Notifications.EXTRA_MESSAGE_ACTION_NAME, getString(R.string.message_cta_default));
                callToActionButton.setText(title);
            } else {
                callToActionButton.setVisibility(View.GONE);
            }

        } else {
            Logger.info("Couldn't retrieve activity_message for ID: " + messageId);
        }
    }

    private void loadAndCustomizeMessage() {
        RequestQueue requestQueue = LiveNationApplication.get().getRequestQueue();
        UrbanAirshipRequest urbanAirshipRequest = new UrbanAirshipRequest(Request.Method.GET, message.getMessageUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String message = response.optString("message");
                if (message != null) {
                    Spannable sp = new SpannableString(Html.fromHtml(message));
                    Linkify.addLinks(sp, Linkify.ALL);
                    messageView.setMovementMethod(LinkMovementMethod.getInstance());
                    messageView.setText(sp);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorView.setVisibility(View.VISIBLE);
            }
        }, RichPushManager.shared().getRichPushUser().getId(), RichPushManager.shared().getRichPushUser().getPassword());
        requestQueue.add(urbanAirshipRequest);
    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
    }


    private class CallToActionClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            Bundle messageExtras = message.getExtras();
            if (!messageExtras.containsKey(Constants.Notifications.EXTRA_MESSAGE_ACTION_URL)) {
                throw new IllegalStateException("CallToActionClickListener.onClick should never be called without an EXTRA_MESSAGE_ACTION_URL");
            }

            String url = messageExtras.getString(Constants.Notifications.EXTRA_MESSAGE_ACTION_URL);

            //Analytics
            Props props = new Props();
            props.put(AnalyticConstants.DEEP_LINK_URL, url);
            LiveNationAnalytics.track(AnalyticConstants.DEEP_LINK_BUTTON_TAP, AnalyticsCategory.NOTIFICATION, props);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }
}
