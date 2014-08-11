package com.livenation.mobile.android.na.cash.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactsCursorAdapter;
import com.livenation.mobile.android.na.cash.model.PhoneNumber;

import java.util.ArrayList;

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
            ArrayList<String> emails = new ArrayList<String>();
            emails.add(completionText);
            return new ContactData("{}", completionText, emails, null, null);
        } else {
            ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
            phoneNumbers.add(new PhoneNumber(completionText, ""));
            return new ContactData("{}", completionText, null, phoneNumbers, null);
        }
    }
}
