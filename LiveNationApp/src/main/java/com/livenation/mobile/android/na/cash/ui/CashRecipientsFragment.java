package com.livenation.mobile.android.na.cash.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.ContactData;
import com.livenation.mobile.android.na.cash.ui.views.ContactsCompleteTextView;
import com.livenation.mobile.android.na.cash.ui.views.TokenCompleteTextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashRecipientsFragment extends Fragment implements TokenCompleteTextView.TokenListener<ContactData> {
    public static final String TAG = CashRecipientsFragment.class.getSimpleName();

    @InjectView(R.id.fragment_cash_recipients_field_to) ContactsCompleteTextView toField;
    @InjectView(R.id.fragment_cash_recipients_field_note) EditText noteField;

    //region Lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
