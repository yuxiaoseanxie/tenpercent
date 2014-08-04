package com.livenation.mobile.android.na.cash;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.PhoneNumber;
import com.livenation.mobile.android.na.cash.model.ContactData;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashRecipientsFragment extends ListFragment {
    public static final String TAG = CashRecipientsFragment.class.getSimpleName();

    @InjectView(R.id.fragment_cash_recipients_field_to) EditText toField;
    @InjectView(R.id.fragment_cash_recipients_field_note) EditText fromField;

    private RecipientAdapter adapter;
    private boolean searching = false;

    private ArrayList<ContactData> selectedContacts = new ArrayList<ContactData>();
    private ArrayList<ContactData> allContacts;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new LoadAllContactsTask(getActivity().getContentResolver()).execute();

        this.adapter = new RecipientAdapter(getActivity());
        setListAdapter(adapter);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_recipients, container, false);
        ButterKnife.inject(this, view);

        toField.setOnEditorActionListener(new EditorActionListener());

        return view;
    }

    //endregion


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ContactData contact = adapter.getItem(position);
        selectedContacts.add(contact);

        searching = false;
        adapter.clear();
        adapter.addAll(selectedContacts);
    }

    private class LoadAllContactsTask extends AsyncTask<Void, Void, ArrayList<ContactData>> {
        private final ContentResolver contentResolver;

        private LoadAllContactsTask(@NonNull ContentResolver contentResolver) {
            this.contentResolver = contentResolver;
        }


        private ArrayList<String> getEmails(@NonNull String id) {
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Email.DATA
            };
            String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
            String[] selectionArgs = new String[] { id };
            Cursor cursorEmail = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                                       projection,
                                                       selection,
                                                       selectionArgs,
                                                       null);

            ArrayList<String> emails = new ArrayList<String>();
            if (cursorEmail.moveToNext()) {
                String email = cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                emails.add(email);
            }
            cursorEmail.close();

            return emails;
        }

        private ArrayList<PhoneNumber> getPhoneNumbers(@NonNull String id) {
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            };
            String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
            String[] selectionArgs = new String[] { id };
            Cursor cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                       projection,
                                                       selection,
                                                       selectionArgs,
                                                       null);

            ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
            if (cursorPhone.moveToNext()) {
                String displayName = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneNumbers.add(new PhoneNumber(number, displayName));
            }
            cursorPhone.close();

            return phoneNumbers;
        }

        @Override
        protected ArrayList<ContactData> doInBackground(Void... voids) {
            String[] projection = new String[] {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            String sortQuery = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                                                  projection,
                                                  null,
                                                  null,
                                                  sortQuery);
            ArrayList<ContactData> accumulator = new ArrayList<ContactData>();
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                ArrayList<String> emails = getEmails(id);
                ArrayList<PhoneNumber> phoneNumbers = getPhoneNumbers(id);
                if (emails.isEmpty() && phoneNumbers.isEmpty()) {
                    continue;
                }

                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                accumulator.add(new ContactData(id, displayName, emails, phoneNumbers));
            }

            return accumulator;
        }

        @Override
        protected void onPostExecute(ArrayList<ContactData> contacts) {
            CashRecipientsFragment.this.allContacts = contacts;
        }
    }

    private class SearchTask extends AsyncTask<String, Void, ArrayList<ContactData>> {
        @Override
        protected ArrayList<ContactData> doInBackground(String... strings) {
            String query = strings[0];

            ArrayList<ContactData> accumulator = new ArrayList<ContactData>();
            contacts: for (ContactData contact : allContacts) {
                if (contact.getName().contains(query)) {
                    accumulator.add(contact);
                    continue;
                }

                for (String email : contact.getEmails()) {
                    if (email.contains(query)) {
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
            searching = true;

            adapter.clear();
            adapter.addAll(contacts);
        }
    }

    private class EditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                new SearchTask().execute(textView.getText().toString());

                textView.setText("");
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

                return true;
            }
            return false;
        }
    }

    class RecipientAdapter extends ArrayAdapter<ContactData> {
        private final LayoutInflater inflater;

        private RecipientAdapter(Context context) {
            super(context, R.layout.list_cash_recipient);

            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.list_cash_recipient, parent, false);
                view.setTag(new ViewHolder(view));
            }

            ViewHolder holder = (ViewHolder) view.getTag();

            ContactData contactData = getItem(position);
            holder.name.setText(contactData.getDisplayName());
            holder.details.setText(contactData.getDetails());

            return view;
        }

        class ViewHolder {
            @InjectView(R.id.list_cash_recipient_name) TextView name;
            @InjectView(R.id.list_cash_recipient_details) TextView details;

            ViewHolder(@NonNull View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
