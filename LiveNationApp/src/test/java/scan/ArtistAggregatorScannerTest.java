package scan;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.helpers.LibraryHelper;
import com.livenation.mobile.android.na.scan.ArtistAggregatorScanner;
import com.livenation.mobile.android.na.scan.ArtistAggregatorScannerCallback;
import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryDump;

import java.util.concurrent.CountDownLatch;

public class ArtistAggregatorScannerTest extends ActivityInstrumentationTestCase2 {

    public ArtistAggregatorScannerTest() {
        super(TestActivity.class);
    }

    public void testScan() {
        final CountDownLatch startApiCall = new CountDownLatch(1);
        final LibraryHelper libraryHelper = new LibraryHelper();
        ArtistAggregatorScanner artistAggregatorScanner = new ArtistAggregatorScanner();
        artistAggregatorScanner.aggregate(getActivity(), new ArtistAggregatorScannerCallback() {
            @Override
            public void onSuccess(LibraryDump libraryDump) {
                startApiCall.countDown();
            }

            @Override
            public void onError(int errorCode, String message) {
                startApiCall.countDown();
            }
        });
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
