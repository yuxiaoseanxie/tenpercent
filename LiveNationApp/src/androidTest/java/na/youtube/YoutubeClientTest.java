package na.youtube;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.toolbox.NoCache;
import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.na.youtube.YouTubeClient;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.tools.NetworkResponseTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import mock.NetworkMock;
import mock.RequestQueueMock;

/**
 * Created by elodieferrais on 1/27/15.
 */
public class YoutubeClientTest extends ActivityInstrumentationTestCase2 {

    private YouTubeClient youTubeClient;
    private NetworkMock network;
    CountDownLatch signal;

    public YoutubeClientTest() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        youTubeClient = new YouTubeClient("youtube");
        //Define a custom network to avoid the server call
        network = new NetworkMock();
        Cache cache = new NoCache();
        RequestQueueMock requestQueueMock = new RequestQueueMock(network, cache);
        youTubeClient.setRequestQueue(requestQueueMock);
    }

    public void testGetArtistBlackList() throws InterruptedException {
        network.setNetworkResponse(NetworkResponseTest.getConfigFileResponseSample(getInstrumentation().getContext()));
        assertTrue(true);
        youTubeClient.getArtistBlackList(new BasicApiCallback<List<String>>() {
            @Override
            public void onResponse(List<String> response) {
                Log.d("Elodie", response.toString());
                assertTrue(true);
                signal.countDown();
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                fail(error.getMessage());
                signal.countDown();
            }
        });
        signal.await();
    }


}
