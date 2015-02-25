package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.DefaultImageHelper;
import com.livenation.mobile.android.na.helpers.SlidingTabLayout;
import com.livenation.mobile.android.na.ui.fragments.ShowTipsFragment;
import com.livenation.mobile.android.na.ui.fragments.VenueFragment;
import com.livenation.mobile.android.na.ui.support.BoxOfficeTabs;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Created by elodieferrais on 2/24/15.
 */
public class ShowActivityV2 extends DetailBaseFragmentActivity {

    private SlidingTabLayout tabs;
    private ViewPager pager;

    private static SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("MMM d", Locale.US);
    public static final String PARAMETER_EVENT_ID = "event_id";
    public static final String PARAMETER_EVENT_CACHED = "event_cached";
    private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"mobile_detail", "tap"};

    private Event event;

    private TransitioningImageView artistImageView;
    private TextView artistTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v2_show);

        artistImageView = (TransitioningImageView) findViewById(R.id.show_image);
        artistTitleTextView = (TextView) findViewById(R.id.show_artist_title);

        Long eventId = null;
        if (args.containsKey(PARAMETER_EVENT_ID)) {
            String eventIdRaw = args.getString(PARAMETER_EVENT_ID);
            eventId = DataModelHelper.getNumericEntityId(eventIdRaw);
        }

        //Use cached event for avoiding the blank page while we are waiting for the http response
        if (args.containsKey(PARAMETER_EVENT_CACHED)) {
            Event event = (Event) args.getSerializable(PARAMETER_EVENT_CACHED);
            setEvent(event);
        } else if (eventId != null) {
            LiveNationApplication.getLiveNationProxy().getSingleEvent(eventId, new BasicApiCallback<Event>() {
                @Override
                public void onResponse(Event event) {
                    setEvent(event);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    //TODO display an error message
                }
            });
        } else {
            finish();
            return;
        }
        artistImageView.setDefaultImage(DefaultImageHelper.computeDefaultDpDrawableId(this, eventId));

        this.pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager()));

        this.tabs = (SlidingTabLayout) findViewById(R.id.slidingtab);
        tabs.setViewPager(pager);
        int tabAccentColor = getResources().getColor(R.color.tab_accent_color);
        tabs.setBottomBorderColor(tabAccentColor);
        tabs.setSelectedIndicatorColors(tabAccentColor);

    }

    private void setEvent(Event event) {
        if (event == null) {
            return;
        }
        this.event = event;
        refresh();
    }

    private void refresh() {
        //Set image and artist title
        String imageUrl = null;
        for (Artist lineup : event.getLineup()) {
                String imageKey = lineup.getBestImageKey(IMAGE_PREFERRED_SHOW_KEYS);
                if (null != imageKey) {
                    imageUrl = lineup.getImageURL(imageKey);
                    break;
                }
        }
        if (null != imageUrl) {
            artistImageView.setImageUrl(imageUrl, LiveNationApplication.get().getImageLoader(), TransitioningImageView.LoadAnimation.FADE_ZOOM);
        }
        artistTitleTextView.setText(event.getName());



    }

    //region Share Overrides

    @Override
    protected void onShare() {
        Props props = new Props();
        if (this.event != null) {
            props.put(AnalyticConstants.EVENT_NAME, event.getName());
            props.put(AnalyticConstants.EVENT_ID, event.getId());
        }
        trackActionBarAction(AnalyticConstants.SHARE_ICON_TAP, props);
        super.onShare();
    }

    @Override
    protected boolean isShareAvailable() {
        return (event != null);
    }

    @Override
    protected String getShareSubject() {
        return event.getName();
    }

    @Override
    protected String getShareText() {
        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        SHORT_DATE_FORMATTER.setTimeZone(timeZone);

        String eventTemplate = getString(R.string.share_template_show);
        return eventTemplate.replace("$HEADLINE_ARTIST", event.getDisplayName())
                .replace("$SHORT_DATE", SHORT_DATE_FORMATTER.format(event.getLocalStartTime()))
                .replace("$VENUE", event.getVenue().getName())
                .replace("$LINK", event.getWebUrl());
    }

    //endregion

    @Override
    protected Map<String, Object> getOmnitureProductsProps() {
        if (args.containsKey(PARAMETER_EVENT_ID)) {
            HashMap cdata = new HashMap<String, Object>();
            cdata.put("&&products", ";" + DataModelHelper.getNumericEntityId(args.getString(PARAMETER_EVENT_ID)));
            return cdata;
        }
        return null;
    }

    public static Bundle getArguments(String eventIdRaw) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_EVENT_ID, eventIdRaw);
        return bundle;
    }

    public static Bundle getArguments(Event event) {
        if (event == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putString(PARAMETER_EVENT_ID, event.getId());
        if (null != event) {
            bundle.putSerializable(PARAMETER_EVENT_CACHED, event);
        }
        return bundle;


    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_SDP;
    }

    @Override
    protected Map<String, Object> getAnalyticsProps() {
        Map<String, Object> props = new HashMap<String, Object>();

        if (args.containsKey(PARAMETER_EVENT_ID)) {
            props.put(AnalyticConstants.EVENT_ID, DataModelHelper.getNumericEntityId(args.getString(PARAMETER_EVENT_ID)));
        }
        if (event != null) {
            props.put(AnalyticConstants.EVENT_ID, event.getNumericId());

            if (event.getVenue() != null) {
                props.put(AnalyticConstants.VENUE_ID, event.getVenue().getNumericId());
            }
            if (event.getLineup() != null && event.getLineup().size() > 0) {
                props.put(AnalyticConstants.ARTIST_ID, event.getLineup().get(0).getNumericId());
            }
        }
        return props;
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_SDP;
    }

    private void trackActionBarAction(String event, Props props) {
        if (props == null) {
            props = new Props();
        }
        props.put(AnalyticConstants.SOURCE, AnalyticsCategory.SDP);
        LiveNationAnalytics.track(event, AnalyticsCategory.ACTION_BAR);
    }

    private class TabFragmentAdapter extends FragmentStatePagerAdapter {
        public TabFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return BoxOfficeTabs.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.show_tips_title);
            } else if (position == 1) {
                return getString(R.string.show_venue_info_title);
            }
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ShowTipsFragment();
            } else if (position == 1) {
                VenueFragment venueFragment = new VenueFragment();
                venueFragment.setVenue(event.getVenue());
                return venueFragment;
            }

            return null;
        }
    }
}
