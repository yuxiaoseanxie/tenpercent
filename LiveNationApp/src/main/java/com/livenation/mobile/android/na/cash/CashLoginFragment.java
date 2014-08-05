package com.livenation.mobile.android.na.cash;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.cash.service.SquareService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CashLoginFragment extends Fragment {
    @InjectView(R.id.fragment_cash_login_phone) EditText phone;

    private final CashLoadingDialogFragment loadingDialogFragment = new CashLoadingDialogFragment();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_login, container, false);
        ButterKnife.inject(this, view);

        phone.setOnEditorActionListener(new PhoneListener());

        return view;
    }


    private CashRequestDetailsActivity getCashRequestDetailsActivity() {
        return (CashRequestDetailsActivity) getActivity();
    }

    private void retrieveCustomerStatus() {
        SquareService.getInstance().retrieveCustomerStatus(new SquareService.ApiCallback<CashCustomerStatus>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(CashLoginFragment.class.getSimpleName(), "Could not retrieve customer status", error);
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

        SquareService.getInstance().startSession(null, phone.getText().toString(), new SquareService.ApiCallback<CashSession>() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(CashLoginFragment.class.getSimpleName(), "Could not login", error);
                loadingDialogFragment.dismiss();
            }

            @Override
            public void onResponse(CashSession response) {
                retrieveCustomerStatus();
            }
        });
    }


    private class PhoneListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

                login();

                return true;
            }

            return false;
        }
    }
}
