package com.livenation.mobile.android.na.cash.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactDataAdapter;
import com.livenation.mobile.android.na.cash.model.DataCallback;
import com.livenation.mobile.android.na.cash.model.LoadAllContactsAysncTask;
import com.livenation.mobile.android.na.cash.model.PhoneNumber;
import com.livenation.mobile.android.na.cash.model.ContactData;

import java.util.ArrayList;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CashRecipientsFragment extends ListFragment implements ContactDataAdapter.DataProvider {
    public static final String TAG = CashRecipientsFragment.class.getSimpleName();
    private static final long SEARCH_DELAY = 400;

    @InjectView(R.id.fragment_cash_recipients_field_to) EditText toField;
    @InjectView(R.id.fragment_cash_recipients_field_note) EditText noteField;
    @InjectView(R.id.fragment_cash_recipients_clear_to) ImageButton clearToField;

    private ContactDataAdapter recipientsAdapter;
    private ArrayList<ContactData> allContacts;
    private HashSet<ContactData> selectedContacts = new HashSet<ContactData>();

    private Handler searchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            new SearchTask().execute(toField.getText().toString());
        }
    };

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new LoadAllContactsAysncTask(getActivity().getContentResolver(), new DataCallback<ArrayList<ContactData>>() {
            @Override
            public void onDataReady(ArrayList<ContactData> contacts) {
                allContacts = contacts;
                recipientsAdapter.addAll(allContacts);

                toField.setEnabled(true);
                noteField.setEnabled(true);
            }
        }).execute();

        this.recipientsAdapter = new ContactDataAdapter(getActivity(), ContactDataAdapter.Mode.SELECTION, this);
        setListAdapter(recipientsAdapter);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_recipients, container, false);
        ButterKnife.inject(this, view);

        ToFieldListener toFieldListener = new ToFieldListener();
        toField.setOnEditorActionListener(toFieldListener);
        toField.addTextChangedListener(toFieldListener);

        return view;
    }

    //endregion


    public CashRecipientsActivity getCashRecipientsActivity() {
        return (CashRecipientsActivity) getActivity();
    }

    public boolean hasContactsSelected() {
        return !selectedContacts.isEmpty();
    }

    public HashSet<ContactData> getSelectedContacts() {
        return selectedContacts;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ContactData contact = recipientsAdapter.getItem(position);
        if (selectedContacts.contains(contact))
            selectedContacts.remove(contact);
        else
            selectedContacts.add(contact);

        getCashRecipientsActivity().invalidateOptionsMenu();
        recipientsAdapter.notifyDataSetChanged();
    }

    private void showSearchResults(ArrayList<ContactData> results) {
        recipientsAdapter.clear();
        recipientsAdapter.addAll(results);
    }

    private void dismissSearchResults() {
        recipientsAdapter.clear();
        recipientsAdapter.addAll(allContacts);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fragment_cash_recipients_clear_to)
    public void clearToField(ImageButton sender) {
        toField.setText("");

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(toField.getWindowToken(), 0);
    }


    @Override
    public boolean isContactSelected(int position, @NonNull ContactData contact) {
        return selectedContacts.contains(contact);
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

    private class SearchTask extends AsyncTask<String, Void, ArrayList<ContactData>> {
        @Override
        protected ArrayList<ContactData> doInBackground(String... strings) {
            String query = strings[0].toLowerCase();

            ArrayList<ContactData> accumulator = new ArrayList<ContactData>();
            contacts: for (ContactData contact : allContacts) {
                if (contact.getName().toLowerCase().contains(query)) {
                    accumulator.add(contact);
                    continue;
                }

                for (String email : contact.getEmails()) {
                    if (email.toLowerCase().contains(query)) {
                        accumulator.add(contact);
                        continue contacts;
                    }
                }

                for (PhoneNumber phoneNumber : contact.getPhoneNumbers()) {
                    if (phoneNumber.getPhoneNumber().contains(query)) {
                        accumulator.add(contact);
                        continue contacts;
                    }
                }
            }

            return accumulator;
        }

        @Override
        protected void onPostExecute(ArrayList<ContactData> contacts) {
            showSearchResults(contacts);
        }
    }

    private class ToFieldListener implements TextView.OnEditorActionListener, TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

        @Override
        public void afterTextChanged(Editable editable) {
            searchHandler.removeMessages(0);

            if (editable.length() == 0) {
                dismissSearchResults();
                clearToField.setVisibility(View.GONE);
            } else {
                searchHandler.sendEmptyMessageDelayed(0, SEARCH_DELAY);
                clearToField.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchHandler.removeMessages(0);
                if (textView.getText().length() == 0) {
                    dismissSearchResults();
                } else {
                    searchHandler.sendEmptyMessage(0);
                }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

                return true;
            }
            return false;
        }
    }

}
