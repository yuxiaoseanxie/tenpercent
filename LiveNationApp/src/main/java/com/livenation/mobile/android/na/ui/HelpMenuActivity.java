package com.livenation.mobile.android.na.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.adapters.HelpListAdapter;
import com.livenation.mobile.android.na.ui.fragments.HelpMenuFragment;
import com.livenation.mobile.android.na.ui.fragments.WebViewFragment;

/**
 * Created by elodieferrais on 4/28/14.
 */
public class HelpMenuActivity extends LiveNationFragmentActivity implements AdapterView.OnItemClickListener {

    private WebViewFragment currentfragment;
    private HelpMenuFragment helpMenuFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_menu);

        helpMenuFragment = (HelpMenuFragment) getFragmentManager().findFragmentById(R.id.activity_menu_fragment);
        helpMenuFragment.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HelpListAdapter adapter = (HelpListAdapter) helpMenuFragment.getListAdapter();
        String slug = adapter.getItem(position).slug;
        String url = getString(R.string.help_base_url_section) + slug;
        currentfragment = WebViewFragment.newInstance(url);
        getFragmentManager().beginTransaction().add(android.R.id.content, currentfragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }

    @Override
    public void onBackPressed() {
        if (currentfragment != null) {
            getFragmentManager().popBackStack();
            currentfragment = null;
        } else {
            super.onBackPressed();
        }
    }
}
