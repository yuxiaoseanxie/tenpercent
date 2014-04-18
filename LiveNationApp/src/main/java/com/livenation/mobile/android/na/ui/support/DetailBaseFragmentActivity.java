package com.livenation.mobile.android.na.ui.support;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.ui.LiveNationFragmentActivity;
import com.livenation.mobile.android.na.ui.SearchActivity;

public abstract class DetailBaseFragmentActivity extends LiveNationFragmentActivity {
    private MenuItem shareItem;

    //region Menus

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_base_menu, menu);

        shareItem = menu.findItem(R.id.action_share);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                onSearch();
                return true;

            case R.id.action_share:
                onShare();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(shareItem == null)
            throw new IllegalStateException("Subclasses of DetailBaseFragmentActivity must call super.onCreateOptionsMenu");

        shareItem.setEnabled(isShareAvailable());

        return true;
    }

    //endregion


    //region Actions

    protected void onShare() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getShareSubject());
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareText());
        startActivity(Intent.createChooser(shareIntent, getShareIntentChooserTitle()));
    }

    protected void onSearch() {
        LiveNationAnalytics.track(AnalyticConstants.SEARCH_ICON_TAP);
        startActivity(new Intent(this, SearchActivity.class));
    }

    //endregion


    //region Sharing Overrides

    protected String getShareIntentChooserTitle() {
        return getString(R.string.share_chooser_title);
    }
    protected abstract boolean isShareAvailable();
    protected abstract String getShareSubject();
    protected abstract String getShareText();

    //endregion
}
