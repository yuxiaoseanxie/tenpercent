package com.livenation.mobile.android.na.ui.dialogs;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.adapters.TravelAdapter;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import rx.subjects.Subject;

/**
 * Created by elodieferrais on 11/25/14.
 */
public abstract class TravelListPopupWindow extends ListPopupWindow {
    public TravelListPopupWindow(Activity activity, View anchor, Subject fastestUber) {
        super(activity);

        final int width = activity.getResources().getDimensionPixelSize(R.dimen.venue_travel_popup_width);
        setWidth(width);
        setAdapter(new TravelAdapter(activity, fastestUber));
        setAnchorView(anchor);
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TravelOption option = TravelOption.values()[position];
                onOptionClicked(option);
            }
        });
    }

    abstract public void onOptionClicked(TravelOption travelOption);

    public static enum TravelOption {uber, maps}

}
