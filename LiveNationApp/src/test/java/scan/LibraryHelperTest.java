package scan;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.helpers.LibraryHelper;
import com.livenation.mobile.android.na.scan.ArtistAggregatorScanner;
import com.livenation.mobile.android.na.scan.ArtistAggregatorScannerCallback;
import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryDump;

import java.util.concurrent.CountDownLatch;


public class LibraryHelperTest extends ActivityInstrumentationTestCase2 {

    public LibraryHelperTest() {
        super(TestActivity.class);
    }

    public void testAggregate() {
        final CountDownLatch startApiCall = new CountDownLatch(1);
        final LibraryHelper libraryHelper = new LibraryHelper();
        ArtistAggregatorScanner artistAggregatorScanner = new ArtistAggregatorScanner();
        artistAggregatorScanner.aggregate(getActivity(), new ArtistAggregatorScannerCallback() {
            @Override
            public void onSuccess(LibraryDump libraryDump) {
                libraryHelper.sendLibraryScan(libraryDump, new LiveNationApiService.SendLibraryAffinitiesCallback() {
                    @Override
                    public void onSuccess(int artistCount) {
                        startApiCall.countDown();
                    }

                    @Override
                    public void onFailure(int errorCode, String message) {
                        startApiCall.countDown();
                        fail();
                    }
                });
            }

            @Override
            public void onError(int errorCode, String message) {
                startApiCall.countDown();
                fail();
            }
        });
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}
