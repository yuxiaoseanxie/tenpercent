package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Entity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameterDefinitions;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.MultiGetParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.segment.android.models.Props;

import java.util.List;
import java.util.Set;

public class UrlActivity extends LiveNationFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);

        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            dispatchLiveNationIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            dispatchLiveNationIntent(intent);
        }
    }


    //region Error Handling

    public void displayError(int reasonStringId) {
        Toast.makeText(getApplicationContext(), reasonStringId, Toast.LENGTH_SHORT).show();
    }

    //endregion


    //region Ticketmaster Urls

    public MultiGetParameters buildMultiGetParameters(String typedId) {
        MultiGetParameters multiGetParameters = new MultiGetParameters();
        multiGetParameters.setType(ApiParameterDefinitions.MultiGet.IdType.TICKETMASTER);
        multiGetParameters.addId(typedId);
        return multiGetParameters;
    }

    public <T> void fetchEntity(final String id, final Response.Listener<T> success) {
        MultiGetParameters multiGetParameters = buildMultiGetParameters(id);
        LiveNationApplication.getLiveNationProxy().multiGet(multiGetParameters, new BasicApiCallback<List<Entity>>() {
            @Override
            public void onResponse(List<Entity> response) {
                if (response.size() == 0) {
                    displayError(R.string.url_error_bad_entity);

                    finish();
                    return;
                }

                T entity = (T) response.get(0);
                success.onResponse(entity);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                Log.e(getClass().getName(), "multi-get failed: " + error);
                displayError(R.string.url_error_platform);
                finish();
            }
        });
    }


    // Handles Urls of forms livenation:///event/:id
    //                       livenation:///events/:id
    public void dispatchEvent(Uri data) {
        String id = Event.makeTypedId(data.getLastPathSegment());

        fetchEntity(id, new Response.Listener<Event>() {
            @Override
            public void onResponse(Event event) {
                Intent intent = new Intent(UrlActivity.this, ShowActivity.class);
                Bundle extras = ShowActivity.getArguments(event);
                ;
                intent.putExtras(extras);
                startActivity(intent);
                finish();
            }
        });
    }

    // Handles Urls of forms livenation:///artist/:id
    //                       livenation:///artist/:id/events
    //                       livenation:///artists/:id
    //                       livenation:///artists/:id/events
    public void dispatchArtist(Uri data) {
        List<String> pathSegments = data.getPathSegments();
        String id = Artist.makeTypedId(data.getLastPathSegment());

        // Handle paths like `/artist/:id/events`
        if (pathSegments.size() == 3) {
            id = Artist.makeTypedId(pathSegments.get(1));
        }

        fetchEntity(id, new Response.Listener<Artist>() {
            @Override
            public void onResponse(Artist artist) {
                Intent intent = new Intent(UrlActivity.this, ArtistActivity.class);
                intent.putExtras(SingleArtistPresenter.getAruguments(artist.getId()));
                SingleArtistPresenter.embedResult(intent.getExtras(), artist);
                startActivity(intent);
                finish();
            }
        });
    }

    //endregion


    //region Navigate Urls

    public boolean isNavigate(Uri data) {
        List<String> pathSegments = data.getPathSegments();
        if (pathSegments.size() > 0)
            return pathSegments.get(0).equals("navigate");
        else
            return false;
    }

    // Handles Urls of forms livenation:///navigate/:typed_id
    public void dispatchNavigate(Uri data) {
        String id = data.getLastPathSegment();

        Intent intent = null;
        if (id.startsWith("evt_")) {
            intent = new Intent(this, ShowActivity.class);
            intent.putExtras(ShowActivity.getArguments(id));
        } else if (id.startsWith("art_")) {
            intent = new Intent(this, ArtistActivity.class);
            intent.putExtras(SingleArtistPresenter.getAruguments(id));
        } else if (id.startsWith("ven_")) {
            intent = new Intent(this, VenueActivity.class);
            intent.putExtras(SingleVenuePresenter.getAruguments(id));
        } else {
            Log.i(getClass().getName(), "Unhandled incoming url " + data);
            displayError(R.string.url_error_bad_url);

            finish();
        }

        startActivity(intent);
        finish();
    }

    //endregion


    public void dispatchLiveNationIntent(Intent intent) {
        final Uri data = buildUri(intent.getData());
        if (isNavigate(data)) {
            dispatchNavigate(data);
        } else {
            trackDeepLinks(data);
            List<String> pathSegments = data.getPathSegments();
            if (pathSegments.size() > 0 && (pathSegments.get(0).equals("event") || pathSegments.get(0).equals("events"))) {
                dispatchEvent(data);
            } else if (pathSegments.size() > 0 && (pathSegments.get(0).equals("artist") || pathSegments.get(0).equals("artists"))) {
                dispatchArtist(data);
            } else {
                Intent intentToOpen = new Intent(UrlActivity.this, OnBoardingActivity.class);
                startActivity(intentToOpen);
                finish();
            }
        }
    }

    private Uri buildUri(Uri uri) {
        if (TextUtils.isEmpty(uri.getHost())) {
            return uri;
        }
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("livenation").authority("");
        builder.appendEncodedPath(uri.getHost());
        for (int i = 0; i < uri.getPathSegments().size(); i++) {
            builder.appendEncodedPath(uri.getPathSegments().get(i));
        }

        Set<String> parameterNames = uri.getQueryParameterNames();
        for (String paramKey : parameterNames) {
            builder.appendQueryParameter(paramKey, uri.getQueryParameter(paramKey));
        }
        return builder.build();
    }

    private void trackDeepLinks(Uri uri) {
        String btid = uri.getQueryParameter("btid");
        String ui = uri.getQueryParameter("ui");
        String c = uri.getQueryParameter("c");
        String from = uri.getQueryParameter("from");
        String url = uri.toString();
        Props props = new Props();
        props.put(AnalyticConstants.BTID, btid);
        props.put(AnalyticConstants.UI, ui);
        props.put(AnalyticConstants.C, c);
        props.put(AnalyticConstants.FROM, from);
        props.put(AnalyticConstants.DEEP_LINK_URL, url);

        LiveNationAnalytics.track(AnalyticConstants.DEEP_LINK_REDIRECTION, AnalyticsCategory.HOUSEKEEPING, props);

    }
}
