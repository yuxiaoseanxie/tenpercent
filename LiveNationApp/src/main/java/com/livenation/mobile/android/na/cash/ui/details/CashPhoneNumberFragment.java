package com.livenation.mobile.android.na.cash.ui.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;
import com.livenation.mobile.android.na.cash.ui.CashLoadingDialogFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashPhoneNumberFragment extends Fragment {
    @InjectView(R.id.fragment_cash_phone_number_text) EditText phone;

    private final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_phone_number, container, false);
        ButterKnife.inject(this, view);

        phone.setOnEditorActionListener(new PhoneListener());

        return view;
    }


    private CashRequestDetailsActivity getCashRequestDetailsActivity() {
        return (CashRequestDetailsActivity) getActivity();
    }

    private void retrieveCustomerStatus() {
        SquareCashService.getInstance().retrieveCustomerStatus(new SquareCashService.ApiCallback<CashCustomerStatus>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(CashUtils.LOG_TAG, "Could not retrieve customer status", error);
                loadingDialogFragment.dismiss();
            }

            @Override
            public void onResponse(CashCustomerStatus response) {
                loadingDialogFragment.dismiss();
                getCashRequestDetailsActivity().continueWithCustomerStatus(response);
            }
        });
    }

    private void login() {
        loadingDialogFragment.show(getFragmentManager(), CashLoadingDialogFragment.TAG);

        SquareCashService.getInstance().startSession(null, phone.getText().toString(), new SquareCashService.ApiCallback<CashSession>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(CashUtils.LOG_TAG, "Could not login", error);
                loadingDialogFragment.dismiss();
            }

            @Override
            public void onResponse(CashSession response) {
                getCashRequestDetailsActivity().setPhoneNumber(phone.getText().toString());
                retrieveCustomerStatus();
            }
        });
    }


    private class PhoneListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                CashUtils.dismissKeyboard(textView);

                login();

                return true;
            }

            return false;
        }
    }
}
