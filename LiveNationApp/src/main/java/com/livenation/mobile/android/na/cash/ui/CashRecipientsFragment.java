package com.livenation.mobile.android.na.cash.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.model.ContactDataAdapter;
import com.livenation.mobile.android.na.cash.model.DataCallback;
import com.livenation.mobile.android.na.cash.model.LoadAllContactsAsyncTask;
import com.livenation.mobile.android.na.cash.ui.views.ContactsCompleteTextView;
import com.livenation.mobile.android.na.cash.ui.views.TokenCompleteTextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashRecipientsFragment extends ListFragment implements ContactDataAdapter.DataProvider, TokenCompleteTextView.TokenListener<ContactData> {
    public static final String TAG = CashRecipientsFragment.class.getSimpleName();

    @InjectView(R.id.fragment_cash_recipients_field_to) ContactsCompleteTextView toField;
    @InjectView(R.id.fragment_cash_recipients_field_note) EditText noteField;

    private ContactDataAdapter recipientsAdapter;
    private ArrayList<ContactData> allContacts;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new LoadAllContactsAsyncTask(getActivity().getContentResolver(), new DataCallback<ArrayList<ContactData>>() {
            @Override
            public void onDataReady(ArrayList<ContactData> contacts) {
                allContacts = contacts;
                recipientsAdapter.addAll(allContacts);

                toField.setAdapter(new ContactDataAdapter(getActivity(), CashRecipientsFragment.this, contacts));
                noteField.setEnabled(true);
            }
        }).execute();

        this.recipientsAdapter = new ContactDataAdapter(getActivity(), this);
        setListAdapter(recipientsAdapter);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_recipients, container, false);
        ButterKnife.inject(this, view);

        toField.setTokenListener(this);

        return view;
    }

    //endregion


    public CashRecipientsActivity getCashRecipientsActivity() {
        return (CashRecipientsActivity) getActivity();
    }

    public boolean hasContactsSelected() {
        return !toField.getObjects().isEmpty();
    }

    public ArrayList<ContactData> getSelectedContacts() {
        return toField.getObjects();
    }

    public String getNote() {
        return noteField.getText().toString();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ContactData contact = recipientsAdapter.getItem(position);
        toField.addObject(contact);

        getCashRecipientsActivity().invalidateOptionsMenu();
    }


    @Override
    public void onTokenAdded(ContactData token) {
        getCashRecipientsActivity().invalidateOptionsMenu();
    }

    @Override
    public void onTokenRemoved(ContactData token) {
        getCashRecipientsActivity().invalidateOptionsMenu();
    }


    @NonNull
    @Override
    public String getSmallDetails(int position, @NonNull ContactData contact) {
        return contact.getDetails();
    }

    @NonNull
    @Override
    public String getBigDetails(int position, @NonNull ContactData contact) {
        return "";
    }
}
