package com.livenation.mobile.android.na.scan;

import android.content.Context;

import com.livenation.mobile.android.na.scan.aggregators.ArtistAggregator;
import com.livenation.mobile.android.na.scan.aggregators.ArtistAggregatorCallback;
import com.livenation.mobile.android.na.scan.aggregators.DeviceArtistAggregator;
import com.livenation.mobile.android.na.scan.aggregators.GooglePlayMusicArtistAggregator;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibraryEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArtistAggregatorScanner {

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

    public void aggregate(Context context, ArtistAggregatorScannerCallback callback, Date sinceDate) {
        ScannerTask task = new ScannerTask(context,callback, sinceDate);
        new Thread(task).start();
    }

    public void aggregate(Context context, ArtistAggregatorScannerCallback callback) {
        aggregate(context, callback, null);
    }

    private ArtistAggregator getInstanceOfArtistAggregator(Aggregator aggregator, Context context) {
        try {
            Constructor constructor = aggregator.getAggregatorClass().getConstructor(new Class[]{Context.class});
            ArtistAggregator artistAggregator = (ArtistAggregator) constructor.newInstance(context);
            return artistAggregator;
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException(ArtistAggregatorScanner.class.getSimpleName() + ":NoSuchMethodException:" + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new UnsupportedOperationException(ArtistAggregatorScanner.class.getSimpleName() + ":NoSuchMethodException:" + e.getMessage());
        } catch (InstantiationException e) {
            throw new UnsupportedOperationException(ArtistAggregatorScanner.class.getSimpleName() + ":NoSuchMethodException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(ArtistAggregatorScanner.class.getSimpleName() + ":NoSuchMethodException:" + e.getMessage());
        }
    }

    private class ScannerTask implements Runnable {

        final private Set<Aggregator> aggregators = new HashSet<Aggregator>();
        final private MusicLibrary musicLibrary = new MusicLibrary();
        final private Context context;
        final private ArtistAggregatorScannerCallback callback;
        final private Date sinceDate;

        public ScannerTask(Context context, ArtistAggregatorScannerCallback callback) {
            this(context, callback, null);
        }

        public ScannerTask(Context context, ArtistAggregatorScannerCallback callback, Date sinceDate) {
            if (context == null) {
                throw new NullPointerException("Context cannot be null");
            }
            if (callback == null) {
                throw new NullPointerException("ArtistAggregatorScannerCallback cannot be null");
            }
            this.context = context;
            this.callback = callback;
            this.sinceDate = sinceDate;
        }

        @Override
        public void run() {
            for (Aggregator aggregator : Aggregator.values()) {
                if (aggregator.isScannable()) {
                    aggregators.add(aggregator);
                }
            }

            //Need a copy to be able to remove aggregator from this list when it's done.
            //If we don't do that, we get an ConcurrentModificationException
            final Set<Aggregator> aggregatorsCopy = new HashSet<Aggregator>(aggregators);
            for (Aggregator aggregator : aggregatorsCopy) {
                ArtistAggregator artistAggregator = getInstanceOfArtistAggregator(aggregator, ScannerTask.this.context);
                final Aggregator aggregatorFinal = aggregator;
                artistAggregator.getArtists(sinceDate, new ArtistAggregatorCallback() {
                    @Override
                    public void onResult(List<MusicLibraryEntry> libraryEntries) {
                        if (libraryEntries != null) {
                            musicLibrary.addAll(libraryEntries);
                        }
                        decrementJobCounter(aggregatorFinal);
                    }
                });
            }
        }

        private void decrementJobCounter(Aggregator aggregator) {
            aggregators.remove(aggregator);
            if (aggregators.isEmpty()) {
                callback.onSuccess(musicLibrary);
            }
        }
    }
}
