package com.livenation.mobile.android.na.youtube;

import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.mock.NetworkMock;
import com.mock.RequestQueueMock;
import com.tools.NetworkResponseTest;
import com.tools.ObjectTest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.test.InstrumentationTestCase;

/**
 * Created by elodieferrais on 1/27/15.
 */
public class YoutubeClientTest extends InstrumentationTestCase {

    private YouTubeClient youTubeClient;
    private NetworkMock network;
    CountDownLatch signal;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        youTubeClient = new YouTubeClient("youtube");
        //Define a custom network to avoid the server call
        network = new NetworkMock();
        RequestQueueMock requestQueueMock = new RequestQueueMock(network);
        youTubeClient.setRequestQueue(requestQueueMock);
        signal = new CountDownLatch(1);
    }

    public void testGetArtistBlackList() throws InterruptedException {
        network.setNetworkResponse(NetworkResponseTest.getConfigFileResponseSample(getInstrumentation().getContext()));
        assertTrue(true);
        youTubeClient.getArtistBlackList(new BasicApiCallback<List<String>>() {
            @Override
            public void onResponse(List<String> response) {
                JSONObject configFile = ObjectTest.getConfigFileSample(getInstrumentation().getContext());
                JSONObject youtube = configFile.optJSONObject("youtube");
                JSONArray jsonArray = youtube.optJSONArray("blacklisted_artist_ids");

                for (int i = 0; i < jsonArray.length(); i++) {
                    assertTrue(response.contains(jsonArray.optString(i)));
                    response.remove(jsonArray.optString(i));
                }
                assertTrue(response.isEmpty());
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
