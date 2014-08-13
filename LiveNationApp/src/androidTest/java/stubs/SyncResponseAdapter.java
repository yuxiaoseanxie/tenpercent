package stubs;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncResponseAdapter<T> implements Response.Listener<T>, Response.ErrorListener {
    private static final long WAIT_TIMEOUT_MS = 5000;

    private final CountDownLatch signal = new CountDownLatch(1);

    private VolleyError error;
    private T response;


    @Override
    public void onErrorResponse(VolleyError error) {
        this.error = error;
        signal.countDown();
    }

    @Override
    public void onResponse(T response) {
        this.response = response;
        signal.countDown();
    }


    public T get() throws VolleyError {
        try {
            signal.await(WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (error != null) {
            throw error;
        } else {
            return response;
        }
    }

    public T getOrFail() {
        try {
            return get();
        } catch (VolleyError e) {
            TestCase.fail("Request failed " + e);
            return null;
        }
    }


    @Override
    public String toString() {
        return "SyncResponseAdapter{" +
                "error=" + error +
                ", response=" + response +
                '}';
    }
}
