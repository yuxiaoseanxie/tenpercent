package com.mock;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;

/**
 * Created by elodieferrais on 1/13/15.
 */
public class NetworkMock extends BasicNetwork {

    private NetworkResponse response;

    public NetworkMock() {
        super(new HurlStack());
    }

    public void setNetworkResponse(NetworkResponse response) {
        this.response = response;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        return response;
    }
}
