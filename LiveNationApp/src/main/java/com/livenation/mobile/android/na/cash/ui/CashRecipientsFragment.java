package com.livenation.mobile.android.na.cash.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomization;
import com.livenation.mobile.android.na.cash.ui.views.ContactsCompleteTextView;
import com.livenation.mobile.android.na.cash.ui.views.TokenCompleteTextView;

import java.util.ArrayList;

public class CashRecipientsFragment extends Fragment implements TokenCompleteTextView.TokenListener<ContactData> {
    public static final String TAG = CashRecipientsFragment.class.getSimpleName();

    private ContactsCompleteTextView toField;
    private EditText noteField;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_recipients, container, false);

        this.toField = (ContactsCompleteTextView) view.findViewById(R.id.fragment_cash_recipients_field_to);
        this.noteField = (EditText) view.findViewById(R.id.fragment_cash_recipients_field_note);

        toField.setTokenListener(this);
        noteField.setFilters(new InputFilter[] { new InputFilter.LengthFilter(CashCustomization.MAX_STATEMENT_LENGTH) });
        noteField.setText(getString(R.string.cash_field_value_note_fmt, getCashRecipientsActivity().getEvent().getDisplayName()));

        return view;
    }

    //endregion


    public CashRecipientsActivity getCashRecipientsActivity() {
        return (CashRecipientsActivity) getActivity();
    }

    public boolean hasContactsSelected() {
        return !toField.getObjects().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ContactData> getSelectedContacts() {
        return toField.getObjects();
    }

    public String getNote() {
        return noteField.getText().toString();
    }


    @Override
    public void onTokenAdded(ContactData token) {
        getCashRecipientsActivity().invalidateOptionsMenu();
    }

    @Override
    public void onTokenRemoved(ContactData token) {
        getCashRecipientsActivity().invalidateOptionsMenu();
    }
}
