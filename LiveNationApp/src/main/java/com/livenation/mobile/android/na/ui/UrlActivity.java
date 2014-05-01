package com.livenation.mobile.android.na.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.ApiHelper;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Entity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameterDefinitions;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.MultiGetParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.List;

public class UrlActivity extends LiveNationFragmentActivity {
    private static final int REQUEST_CODE = 42;

    private static final String EXTRA_HAS_HANDLED_URL = "com.livenation.mobile.android.na.ui.UrlActivity.EXTRA_HAS_HANDLED_URL";
    private boolean hasHandledUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);

        if (savedInstanceState != null)
            hasHandledUrl = savedInstanceState.getBoolean(EXTRA_HAS_HANDLED_URL, false);

        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            handleUrlIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            handleUrlIntent(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(EXTRA_HAS_HANDLED_URL, hasHandledUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            setResult(resultCode);
            finish();
        }
    }

    public void handleTicketmasterEvent(LiveNationApiService service, Uri data) {
        String id = data.getLastPathSegment();
        String typedId = "evt_" + id;

        MultiGetParameters multiGetParameters = new MultiGetParameters();
        multiGetParameters.setType(ApiParameterDefinitions.MultiGet.IdType.TICKETMASTER);
        multiGetParameters.addId(typedId);
        service.multiGet(multiGetParameters, new ApiService.BasicApiCallback<List<Entity>>() {
            @Override
            public void onResponse(List<Entity> response) {
                if (response.size() == 0) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }

                Event event = (Event) response.get(0);
                Intent intent = new Intent(UrlActivity.this, ShowActivity.class);
                intent.putExtras(SingleEventPresenter.getAruguments(event.getId()));
                SingleEventPresenter.embedResult(intent.getExtras(), event);
                startActivityForResult(intent, REQUEST_CODE);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                Log.e(getClass().getName(), "multi-get failed: " + error);

                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    public void handleTicketmasterArtist(LiveNationApiService service, Uri data) {
        List<String> pathSegments = data.getPathSegments();
        String id = data.getLastPathSegment();
        if (pathSegments.size() == 3) {
            id = pathSegments.get(1);
        }

        String typedId = "art_" + id;
        MultiGetParameters multiGetParameters = new MultiGetParameters();
        multiGetParameters.setType(ApiParameterDefinitions.MultiGet.IdType.TICKETMASTER);
        multiGetParameters.addId(typedId);
        service.multiGet(multiGetParameters, new ApiService.BasicApiCallback<List<Entity>>() {
            @Override
            public void onResponse(List<Entity> response) {
                if (response.size() == 0) {
                    setResult(RESULT_CANCELED);
                    finish();

                    return;
                }

                Artist artist = (Artist) response.get(0);
                Intent intent = new Intent(UrlActivity.this, ShowActivity.class);
                intent.putExtras(SingleArtistPresenter.getAruguments(artist.getId()));
                SingleArtistPresenter.embedResult(intent.getExtras(), artist);
                startActivityForResult(intent, REQUEST_CODE);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                Log.e(getClass().getName(), "multi-get failed: " + error);

                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    public void handleNavigate(LiveNationApiService service, Uri data) {
        String id = data.getLastPathSegment();

        Intent intent = null;
        if (id.startsWith("evt_")) {
            intent = new Intent(this, ShowActivity.class);
            intent.putExtras(SingleEventPresenter.getAruguments(id));
        } else if (id.startsWith("art_")) {
            intent = new Intent(this, ArtistActivity.class);
            intent.putExtras(SingleArtistPresenter.getAruguments(id));
        } else if (id.startsWith("ven_")) {
            intent = new Intent(this, VenueActivity.class);
            intent.putExtras(SingleVenuePresenter.getAruguments(id));
        } else {
            Log.i(getClass().getName(), "Unhandled incoming url " + data);
            setResult(RESULT_CANCELED);
            finish();
        }

        startActivityForResult(intent, REQUEST_CODE);
    }

    public void handleUrlIntent(Intent intent) {
        if (hasHandledUrl)
            return;

        final Uri data = intent.getData();
        getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                List<String> pathSegments = data.getPathSegments();
                if (pathSegments.get(0).equals("event") || pathSegments.get(0).equals("events")) {
                    handleTicketmasterEvent(apiService, data);
                } else if (pathSegments.get(0).equals("artist") || pathSegments.get(0).equals("artists")) {
                    handleTicketmasterArtist(apiService, data);
                } else if (pathSegments.get(0).equals("navigate")) {
                    handleNavigate(apiService, data);
                }

                hasHandledUrl = true;
            }

            @Override
            public void onApiServiceNotAvailable() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }


    private ApiHelper getApiHelper() {
        return LiveNationApplication.get().getApiHelper();
    }
}
