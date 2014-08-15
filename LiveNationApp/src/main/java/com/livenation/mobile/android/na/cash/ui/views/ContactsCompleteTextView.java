package com.livenation.mobile.android.na.cash.ui.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactsCursorAdapter;

public class ContactsCompleteTextView extends TokenCompleteTextView<ContactData> {
    private final ContactsCursorAdapter adapter = ContactsCursorAdapter.forAllContacts(getContext());

    public ContactsCompleteTextView(Context context) {
        super(context);
        setAdapter(adapter);
    }

    public ContactsCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdapter(adapter);
    }

    public ContactsCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAdapter(adapter);
    }


    @Override
    protected View getViewForObject(ContactData contact) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View tokenView = inflater.inflate(R.layout.layout_contact_token, null);
        TextView textView = (TextView) tokenView.findViewById(android.R.id.text1);
        textView.setText(contact.getDisplayName());
        return tokenView;
    }

    @Override
    protected Object defaultObject(String completionText) {
        if (Patterns.EMAIL_ADDRESS.matcher(completionText).matches()) {
            return new ContactData("{}", completionText, completionText, null, null);
        } else {
            return new ContactData("{}", completionText, null, completionText, null);
        }
    }

    //region throttler

    /**
     * Throttle keypresses that occur in quick succession so that we reduce the amount of intense
     * DB hits we have to perform
     */
    private static final String KEY_TEXT = "text";
    private static final String KEY_START = "start";
    private static final String KEY_END = "end";
    private static final String KEY_KEY_CODE = "keycode";

    private Handler throttler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String text = msg.getData().getString(KEY_TEXT);
            int start = msg.getData().getInt(KEY_START);
            int end = msg.getData().getInt(KEY_END);
            int keyCode = msg.getData().getInt(KEY_KEY_CODE);

            ContactsCompleteTextView.super.performFiltering(text, start, end, keyCode);
        }
    };

    @Override
    protected void performFiltering(@NonNull CharSequence text, int start, int end, int keyCode) {
        //the cast toString() intentionally creates a copy of the value of the text var, that
        //won't get mutated during the TEXT_CHANGED_POST_DELAY window
        Message message = getMessage(text.toString(), start, end, keyCode);
        throttler.removeMessages(0);
        throttler.sendMessageDelayed(message, Constants.TEXT_CHANGED_POST_DELAY);
    }

    private Message getMessage(String text, int start, int end, int keyCode) {
        Message message = new Message();
        Bundle data = new Bundle();

        data.putString(KEY_TEXT, text);
        data.putInt(KEY_START, start);
        data.putInt(KEY_END, end);
        data.putInt(KEY_KEY_CODE, keyCode);

        message.setData(data);
        return message;
    }

    //endregion
}
