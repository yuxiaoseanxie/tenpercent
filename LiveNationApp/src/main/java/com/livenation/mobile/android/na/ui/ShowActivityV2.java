package com.livenation.mobile.android.na.ui;

import com.apsalar.sdk.Apsalar;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.helpers.DefaultImageHelper;
import com.livenation.mobile.android.na.ui.dialogs.CommerceUnavailableDialogFragment;
import com.livenation.mobile.android.na.ui.dialogs.TicketOfferingsDialogFragment;
import com.livenation.mobile.android.na.ui.fragments.ComingShowFragment;
import com.livenation.mobile.android.na.ui.fragments.ShowFragment;
import com.livenation.mobile.android.na.ui.support.DetailBaseFragmentActivity;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TicketOffering;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.ticketing.Ticketing;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by elodieferrais on 2/24/15.
 */
public class ShowActivityV2 extends DetailBaseFragmentActivity {

    private static SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("MMM d", Locale.US);
    public static final String PARAMETER_EVENT_ID = "event_id";
    public static final String PARAMETER_EVENT_CACHED = "event_cached";
    private final static String[] IMAGE_PREFERRED_SHOW_KEYS = {"mobile_detail", "tap"};

    private Event event;
    private TransitioningImageView artistImageView;
    private TextView artistTitleTextView;
    private Button findTicketsOptions;
    private Button findTickets;
    private GoogleApiClient googleApiClient;
    private Uri appUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_v2_show);

        // Start google app indexing
        googleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.APP_INDEX_API).build();
        googleApiClient.connect();

        artistImageView = (TransitioningImageView) findViewById(R.id.show_image);
        artistTitleTextView = (TextView) findViewById(R.id.show_artist_title);
        findTicketsOptions = (Button) findViewById(R.id.fragment_show_ticketbar_options);
        findTickets = (Button) findViewById(R.id.fragment_show_ticketbar_find);


        Long eventId = null;
        args = getIntent().getExtras();
        if (args != null && args.containsKey(PARAMETER_EVENT_ID)) {
            String eventIdRaw = args.getString(PARAMETER_EVENT_ID);
            eventId = DataModelHelper.getNumericEntityId(eventIdRaw);
        }

        //Use cached event for avoiding the blank page while we are waiting for the http response
        if (args != null && args.containsKey(PARAMETER_EVENT_CACHED)) {
            Event event = (Event) args.getSerializable(PARAMETER_EVENT_CACHED);
            setEvent(event);
        } else if (eventId != null) {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.show_container_pb);
            progressBar.setVisibility(View.VISIBLE);
            LiveNationApplication.getLiveNationProxy().getSingleEvent(eventId, new BasicApiCallback<Event>() {
                @Override
                public void onResponse(Event event) {
                    setEvent(event);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    progressBar.setVisibility(View.GONE);
                    //TODO display an error message
                }
            });
        } else {
            finish();
            return;
        }
        artistImageView.setDefaultImage(DefaultImageHelper.computeDefaultDpDrawableId(this, eventId));

    }

    private void setEvent(Event event) {
        if (event == null) {
            return;
        }
        this.event = event;
        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateUp() {
        Intent intent = new Intent(ShowActivityV2.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

        //Fragment
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_show_detail_container, ComingShowFragment.newInstance(event)).commit();
        //getSupportFragmentManager().beginTransaction().add(R.id.fragment_show_detail_container, ShowFragment.newInstance(event)).commit();


        //Ticket button
        if (event.getTicketOfferings().size() < 2)
            findTicketsOptions.setVisibility(View.GONE);
        else
            findTicketsOptions.setVisibility(View.VISIBLE);
        findTicketsOptions.setOnClickListener(new OnFindTicketsOptionsClick(event));

        OnFindTicketsClick onFindTicketsClick = new OnFindTicketsClick(event);
        findTickets.setOnClickListener(onFindTicketsClick);

        //Action bar
        invalidateIsShareAvailable();

        //Google indexing
        googleViewStart(event);
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
        if (args != null && args.containsKey(PARAMETER_EVENT_ID)) {
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

    @Override
    protected void onStart() {
        super.onStart();
        if (appUrl == null && event != null) {
            googleApiClient.connect();
            googleViewStart(event);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleViewEnd();
        googleApiClient.disconnect();
    }

    @Override
    protected void onSearch() {
        trackActionBarAction(AnalyticConstants.SEARCH_ICON_TAP, null);
        super.onSearch();
    }

    //Ticketing section

    private class OnFindTicketsClick implements View.OnClickListener {
        private final Event event;

        public OnFindTicketsClick(Event event) {
            this.event = event;
        }

        @Override
        public void onClick(final View v) {
            List<TicketOffering> offerings = event.getTicketOfferings();
            if (offerings.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.no_ticket_offerings,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            TicketOffering ticketOffering = offerings.get(0);

            final Props props = AnalyticsHelper.getPropsForEvent(event);
            LiveNationAnalytics.track(AnalyticConstants.FIND_TICKETS_TAP, AnalyticsCategory.SDP, props);

            LiveNationApplication.getAccessTokenProvider().getAccessToken(new BasicApiCallback<AccessToken>() {
                @Override
                public void onResponse(AccessToken response) {
                    Apsalar.event(AnalyticConstants.APSALAR_FIND_TICKET_TAP, AnalyticConstants.TOKEN, response.getToken(), AnalyticConstants.TOKEN_TYPE, response.getType());
                }

                @Override
                public void onErrorResponse(LiveNationError error) {
                    Apsalar.event(AnalyticConstants.APSALAR_FIND_TICKET_TAP);
                }
            });
            showTicketOffering(ticketOffering);
        }
    }

    private class OnFindTicketsOptionsClick implements View.OnClickListener, TicketOfferingsDialogFragment.OnTicketOfferingClickedListener {
        private final Event event;

        private OnFindTicketsOptionsClick(Event event) {
            this.event = event;
        }

        @Override
        public void onClick(View view) {
            Props props = AnalyticsHelper.getPropsForEvent(event);
            LiveNationAnalytics.track(AnalyticConstants.OPTIONS_BUTTON_TAP, AnalyticsCategory.SDP, props);

            TicketOfferingsDialogFragment dialogFragment = TicketOfferingsDialogFragment.newInstance(event);
            dialogFragment.setOnTicketOfferingClickedListener(this);
            dialogFragment.show(getSupportFragmentManager(), "TicketOfferingsDialogFragment");
        }

        @Override
        public void onTicketOfferingClicked(TicketOffering offering) {
            showTicketOffering(offering);
        }
    }

    protected void showTicketOffering(TicketOffering offering) {
        String buyLink = offering.getPurchaseUrl();
        if (Ticketing.isTicketmasterUrl(buyLink)) {
            if (LiveNationApplication.get().getInstalledAppConfig().isCommerceAvailable()) {
                Intent confirmIntent = new Intent(this, OrderConfirmationActivity.class);
                confirmIntent.putExtra(OrderConfirmationActivity.EXTRA_EVENT, event);
                Ticketing.showFindTicketsActivityForUrl(this, confirmIntent, buyLink);
            } else {
                CommerceUnavailableDialogFragment dialogFragment = new CommerceUnavailableDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), CommerceUnavailableDialogFragment.TAG);
            }
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(buyLink)));
            Toast.makeText(this, R.string.tickets_third_party_toast, Toast.LENGTH_SHORT).show();
        }
    }

    //Google
    private void googleViewStart(Event event) {
        Uri webUrl = Uri.parse(getString(R.string.web_url_show) + DataModelHelper.getNumericEntityId(event.getId()));
        String suffixUrl;
        if (event.getId().contains("evt")) {
            suffixUrl = event.getId();
        } else {
            suffixUrl = "evt_" + event.getId();
        }
        appUrl = Uri.parse(getString(R.string.app_url_show) + suffixUrl);

        notifyGoogleViewStart(googleApiClient, webUrl, appUrl, event.getName());

    }

    private void googleViewEnd() {
        notifyGoogleViewEnd(googleApiClient, appUrl);
        appUrl = null;
    }
}
