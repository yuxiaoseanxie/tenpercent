package stubs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.NoCache;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import stubs.converters.JsonConverter;

@SuppressWarnings("UnusedDeclaration")
public class StubHttpStack implements HttpStack {
    private ArrayList<StubResponseProvider> responseProviders = new ArrayList<StubResponseProvider>();
    private RequestQueue requestQueue = null;

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            this.requestQueue = new RequestQueue(new NoCache(), new BasicNetwork(this));
            requestQueue.start();
        }
        return requestQueue;
    }


    //region Handling Requests

    protected StubResponseProvider findProviderForRequest(Request<?> request) {
        for (StubResponseProvider responseProvider : responseProviders) {
            if (responseProvider.matchesRequest(request))
                return responseProvider;
        }
        return null;
    }

    @Override
    public HttpResponse performRequest(Request<?> request,
                                       Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        StubResponseProvider responseProvider = findProviderForRequest(request);
        if (responseProvider != null) {
            return responseProvider.provideResponse(request);
        } else {
            throw new RequestNotAllowedException(request);
        }
    }

    //endregion


    //region Stubbing

    public void addStub(@NonNull StubResponseProvider responseProvider) {
        responseProviders.add(responseProvider);
    }

    public void clearStubs() {
        responseProviders.clear();
    }


    public StubResponseProvider.Builder stubGet(@NonNull String url,
                                                @NonNull Map<String, String> headers) {
        StubResponseProvider.Builder builder = new StubResponseProvider.Builder(this);
        return builder.setMethod(Request.Method.GET)
                .setUrl(url)
                .setOutgoingHeaders(headers);
    }

    public StubResponseProvider.Builder stubDelete(@NonNull String url,
                                                   @NonNull Map<String, String> headers) {
        StubResponseProvider.Builder builder = new StubResponseProvider.Builder(this);
        return builder.setMethod(Request.Method.DELETE)
                .setUrl(url)
                .setOutgoingHeaders(headers);
    }

    public StubResponseProvider.Builder stubPost(@NonNull String url,
                                                 @NonNull Map<String, String> headers,
                                                 @NonNull byte[] body,
                                                 @NonNull String bodyContentType) {
        StubResponseProvider.Builder builder = new StubResponseProvider.Builder(this);
        return builder.setMethod(Request.Method.POST)
                .setUrl(url)
                .setOutgoingHeaders(headers)
                .setOutgoingBody(body)
                .setOutgoingBodyType(bodyContentType);
    }

    public StubResponseProvider.Builder stubPost(@NonNull String url,
                                                 @NonNull Map<String, String> headers,
                                                 @NonNull JSONObject json) {
        return stubPost(url, headers, json.toString().getBytes(), "application/json");
    }

    public StubResponseProvider.Builder stubPost(@NonNull String url,
                                                 @NonNull Map<String, String> headers,
                                                 @Nullable Object json,
                                                 @NonNull JsonConverter converter) {
        try {
            return stubPost(url, headers, converter.convertToJsonString(json).getBytes(), "application/json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public StubResponseProvider.Builder stubPut(@NonNull String url,
                                                @NonNull Map<String, String> headers,
                                                @NonNull byte[] body,
                                                @NonNull String bodyContentType) {
        StubResponseProvider.Builder builder = new StubResponseProvider.Builder(this);
        return builder.setMethod(Request.Method.PUT)
                .setUrl(url)
                .setOutgoingHeaders(headers)
                .setOutgoingBody(body)
                .setOutgoingBodyType(bodyContentType);
    }

    public StubResponseProvider.Builder stubPut(@NonNull String url,
                                                @NonNull Map<String, String> headers,
                                                @NonNull JSONObject json) {
        return stubPut(url, headers, json.toString().getBytes(), "application/json");
    }

    public StubResponseProvider.Builder stubPut(@NonNull String url,
                                                @NonNull Map<String, String> headers,
                                                @Nullable Object json,
                                                @NonNull JsonConverter converter) {
        try {
            return stubPut(url, headers, converter.convertToJsonString(json).getBytes(), "application/json");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Map<String, String> createBasicHeaders(@NonNull String contentType) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", contentType);
        return headers;
    }

    //endregion


    public static class RequestNotAllowedException extends IOException {
        public final Request<?> request;

        public static String getNameForMethod(int method) {
            switch (method) {
                case Request.Method.DEPRECATED_GET_OR_POST:
                    return "DEPRECATED_GET_OR_POST";

                case Request.Method.GET:
                    return "GET";

                case Request.Method.POST:
                    return "POST";

                case Request.Method.PUT:
                    return "PUT";

                case Request.Method.DELETE:
                    return "DELETE";

                case Request.Method.HEAD:
                    return "HEAD";

                case Request.Method.OPTIONS:
                    return "OPTIONS";

                case Request.Method.TRACE:
                    return "TRACE";

                case Request.Method.PATCH:
                    return "PATCH";

                default:
                    return "UNKNOWN";
            }
        }

        public static Map<String, String> safeGetHeaders(@NonNull Request<?> request) {
            try {
                return request.getHeaders();
            } catch (AuthFailureError e) {
                return Collections.emptyMap();
            }
        }

        public RequestNotAllowedException(@NonNull Request<?> request) {
            super("Real outgoing connections are not allowed. Unregistered " + getNameForMethod(request.getMethod())
                    + " request to " + request.getUrl()
                    + " with outgoingHeaders " + safeGetHeaders(request));

            this.request = request;
        }
    }
}
