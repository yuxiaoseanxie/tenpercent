package com.livenation.mobile.android.na.ui.adapters;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.ui.dialogs.TravelListPopupWindow.TravelOption;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import rx.functions.Action1;
import rx.subjects.Subject;

/**
 * Created by elodieferrais on 11/25/14.
 */
public class TravelAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private Subject<?, LiveNationEstimate> fastestUber;

    public TravelAdapter(@NonNull Context context, @NonNull Subject fastestUber) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fastestUber = fastestUber;
    }

    @Override
    public int getCount() {
        return TravelOption.values().length;
    }

    @Override
    public Object getItem(int position) {
        return TravelOption.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TravelOption option = TravelOption.values()[position];
        if (convertView != null) return convertView;
        switch (option) {
            case uber:
                View root = inflater.inflate(R.layout.popup_list_uber, parent, false);
                final TextView text1 = (TextView) root.findViewById(android.R.id.text1);
                final TextView text2 = (TextView) root.findViewById(android.R.id.text2);

                if (AnalyticsHelper.isAppInstalled(ExternalApplicationAnalytics.UBER.getPackageName(), context)) {
                    //hide "get your first ride free!" text
                    text2.setText("");
                    //subscribe to the fastest uber estimate object
                    //this may be ready, or it may not, who knows!
                    fastestUber.subscribe(new Action1<LiveNationEstimate>() {
                        @Override
                        public void call(LiveNationEstimate liveNationEstimate) {
                            if (!liveNationEstimate.hasTime() || !liveNationEstimate.hasPrice())
                                return;
                            String uberTitle = context.getResources().getString(R.string.uber_popup_book_ride_mins);
                            uberTitle = String.format(uberTitle, liveNationEstimate.getTime().getEstimateMins());
                            text1.setText(uberTitle);
                            text2.setText(liveNationEstimate.getPrice().getEstimate());
                        }
                    });
                } else {
                    text2.setText(LiveNationApplication.get().getInstalledAppConfig().getUberFreeRideText());
                }

                return root;
            case maps:
                return inflater.inflate(R.layout.popup_list_maps, parent, false);
        }
        throw new IllegalArgumentException();
    }
}
