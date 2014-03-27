package com.livenation.mobile.android.na.scan;

import android.content.Context;
import android.util.Log;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.scan.aggregators.ArtistAggregator;
import com.livenation.mobile.android.na.scan.aggregators.DeviceArtistAggregator;
import com.livenation.mobile.android.na.scan.aggregators.GooglePlayMusicArtistAggregator;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryDump;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ArtistAggregatorScanner implements Runnable, ApiServiceBinder, LiveNationApiService.SendLibraryAffinitiesCallback {

    private enum Aggregator {
        DEVICE(DeviceArtistAggregator.class),
        GOOGLE_PLAY_MUSIC(GooglePlayMusicArtistAggregator.class);

        private Class<? extends ArtistAggregator> aggregatorClass;

        Aggregator(Class<? extends ArtistAggregator> aggregatorClass) {
            this.aggregatorClass = aggregatorClass;
        }

        public boolean isScannable() {
            //Can add business rules depending on the type of aggregator
            return true;
        }

        public Class<? extends ArtistAggregator> getAggregatorClass() {
            return aggregatorClass;
        }
    }

    private Context context;
    private LibraryDump libraryDump;

    public ArtistAggregatorScanner(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        libraryDump = new LibraryDump();
        for (Aggregator aggregator : Aggregator.values()) {
            if (aggregator.isScannable()) {
                try {
                    Constructor constructor = aggregator.getAggregatorClass().getConstructor(new Class[]{Context.class});
                    ArtistAggregator artistAggregator = (ArtistAggregator) constructor.newInstance(ArtistAggregatorScanner.this.context);
                    if (artistAggregator.getArtists() != null) {
                        libraryDump.addAll(artistAggregator.getArtists());
                    }
                } catch (NoSuchMethodException e) {
                    Log.e(ArtistAggregatorScanner.class.getSimpleName(),
                            "There is no constructor with a Context as parameter in the class " + aggregator.getAggregatorClass().getSimpleName(), e);
                } catch (InvocationTargetException e) {
                    Log.e(ArtistAggregatorScanner.class.getSimpleName(),
                            "Cannot access to the constructor with a Context as parameter in the class " + aggregator.getAggregatorClass().getSimpleName(), e);
                } catch (InstantiationException e) {
                    Log.e(ArtistAggregatorScanner.class.getSimpleName(),
                            "Cannot access to the constructor with a Context as parameter in the class " + aggregator.getAggregatorClass().getSimpleName(), e);
                } catch (IllegalAccessException e) {
                    Log.e(ArtistAggregatorScanner.class.getSimpleName(),
                            "Cannot access to the constructor with a Context as parameter in the class " + aggregator.getAggregatorClass().getSimpleName(), e);
                }
            }
        }
        if (!libraryDump.isEmpty()) {
            LiveNationApplication.get().getApiHelper().bindApi(this);
        }
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        apiService.sendLibraryAffinities(libraryDump, this);
    }

    @Override
    public void onSuccess() {
        Log.d(ArtistAggregator.class.getSimpleName(), "LibraryDump send");
    }

    @Override
    public void onFailure() {
        Log.e(ArtistAggregator.class.getSimpleName(), "LibraryDump failure");
    }

    @Override
    public void onFailure(int errorCode, String message) {
        Log.e(ArtistAggregator.class.getSimpleName(), "LibraryDump failure");
    }
}
