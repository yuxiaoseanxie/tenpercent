package stubs;

import android.support.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.NoCache;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StubHttpStack implements HttpStack {
    private ArrayList<StubResponseProvider> responseProviders = new ArrayList<StubResponseProvider>();

    public RequestQueue newRequestQueue() {
        return new RequestQueue(new NoCache(), new BasicNetwork(this));
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
                      .setHeaders(headers);
    }

    public StubResponseProvider.Builder stubDelete(@NonNull String url,
                                                   @NonNull Map<String, String> headers) {
        StubResponseProvider.Builder builder = new StubResponseProvider.Builder(this);
        return builder.setMethod(Request.Method.DELETE)
                      .setUrl(url)
                      .setHeaders(headers);
    }

    public StubResponseProvider.Builder stubPost(@NonNull String url,
                                                 @NonNull Map<String, String> headers,
                                                 @NonNull byte[] body,
                                                 @NonNull String bodyContentType) {
        StubResponseProvider.Builder builder = new StubResponseProvider.Builder(this);
        return builder.setMethod(Request.Method.POST)
                      .setUrl(url)
                      .setHeaders(headers)
                      .setOutgoingBody(body)
                      .setOutgoingBodyType(bodyContentType);
    }

    public StubResponseProvider.Builder stubPut(@NonNull String url,
                                                @NonNull Map<String, String> headers,
                                                @NonNull byte[] body,
                                                @NonNull String bodyContentType) {
        StubResponseProvider.Builder builder = new StubResponseProvider.Builder(this);
        return builder.setMethod(Request.Method.PUT)
                      .setUrl(url)
                      .setHeaders(headers)
                      .setOutgoingBody(body)
                      .setOutgoingBodyType(bodyContentType);
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
                  + " with headers " + safeGetHeaders(request));

            this.request = request;
        }
    }
}
