package mock;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.NoCache;

/**
 * Created by elodieferrais on 1/13/15.
 */
public class RequestQueueMock extends RequestQueue {
    public RequestQueueMock(Network network, Cache cache) {
        super(cache, network);
    }
}
