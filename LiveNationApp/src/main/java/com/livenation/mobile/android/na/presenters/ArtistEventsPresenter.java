package com.livenation.mobile.android.na.presenters;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.livenation.mobile.android.na.presenters.support.BasePresenter;
import com.livenation.mobile.android.na.presenters.support.BaseResultState;
import com.livenation.mobile.android.na.presenters.support.BaseState;
import com.livenation.mobile.android.na.presenters.support.Presenter;
import com.livenation.mobile.android.na.presenters.views.ArtistEventsView;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArtistEventsPresenter
        extends BasePresenter<ArtistEventsView, ArtistEventsPresenter.ArtistEventsState>
        implements Presenter<ArtistEventsView>, BaseState.StateListener<ArtistEventsPresenter.ArtistEventsState>{
    private static final String INTENT_DATA_KEY = ArtistEventsPresenter.class.getName();
    private static final String PARAMETER_ARTIST_ID = "artist_id";
    private static final String PARAMETER_LIMIT = "limit";

    @Override
    public void initialize(Context context, Bundle args, ArtistEventsView view) {
        ArtistEventsState state = new ArtistEventsState(this, args, view);
        state.run();
    }

    @Override
    public void onStateReady(ArtistEventsState state) {
        super.onStateReady(state);

        ArtistEventsView view = state.getView();
        ArtistEvents events = state.getResult();
        view.setArtistEvents(events);
    }

    public static class ArtistEventsState
            extends BaseResultState<ArtistEvents, ArtistEventsView>
            implements LiveNationApiService.GetEventsApiCallback {
        private ApiParameters.ArtistEventsParameters apiParams;

        public ArtistEventsState(StateListener<ArtistEventsState> listener, Bundle args, ArtistEventsView view) {
            super(listener, args, view);
        }

        @Override
        public void onHasResult(ArtistEvents artistEvents) {
            setResult(artistEvents);
            notifyReady();
        }

        @Override
        public void retrieveResult() {
            getApiService().getArtistEvents(apiParams, this);
        }

        @Override
        public void onFailure(int errorCode, String message) {
            notifyFailed(errorCode);
        }

        @Override
        public void onGetEvents(List<Event> events) {
            double lat = getApiService().getApiConfig().getLat();
            double lng = getApiService().getApiConfig().getLng();
            setResult(ArtistEvents.from((ArrayList<Event>)events, lat, lng));
            notifyReady();
        }

        @Override
        public void applyArgs(Bundle args) {
            super.applyArgs(args);
            apiParams = ApiParameters.createArtistEventsParameters();

            String artistIdRaw = args.getString(PARAMETER_ARTIST_ID);
            apiParams.setArtistId(DataModelHelper.getNumericEntityId(artistIdRaw));

            if (args.containsKey(PARAMETER_LIMIT)) {
                int limit = args.getInt(PARAMETER_LIMIT);
                apiParams.setPage(0, limit);
            }
        }

        @Override
        public String getDataKey() {
            return INTENT_DATA_KEY;
        }
    }

    public static class ArtistEvents implements Serializable {
        private static final double NEARBY_CUTOFF = 50000.0 /* 50km */;
        private static final int MAX_NEARBY = 3;

        private ArrayList<Event> nearby;
        private ArrayList<Event> all;

        public static ArtistEvents from(ArrayList<Event> all, double userLatitude, double userLongitude) {
            ArrayList<Event> nearby = new ArrayList<Event>();
            float[] results = new float[1];
            for (Event event : all) {
                Location.distanceBetween(userLatitude, userLongitude,
                                         Double.valueOf(event.getVenue().getLat()), Double.valueOf(event.getVenue().getLng()),
                                         results);
                if(results[0] <= NEARBY_CUTOFF)
                    nearby.add(event);

                if(nearby.size() >= MAX_NEARBY)
                    break;
            }

            return new ArtistEvents(nearby, all);
        }

        public ArtistEvents(ArrayList<Event> nearby, ArrayList<Event> all) {
            this.nearby = nearby;
            this.all = all;
        }

        public ArrayList<Event> getNearby() {
            return nearby;
        }

        public ArrayList<Event> getAll() {
            return all;
        }
    }
}
