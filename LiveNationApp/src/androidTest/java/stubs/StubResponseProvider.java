package stubs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

import stubs.converters.JsonConverter;

@SuppressWarnings("UnusedDeclaration")
public class StubResponseProvider {
    private final int method;
    private final String url;
    private final Map<String, String> outgoingHeaders;

    private final String outgoingBodyType;
    private final byte[] outgoingBody;

    private final HttpResponse response;
    private final long emulatedLoadTime;


    public StubResponseProvider(int method,
                                @NonNull String url,
                                Map<String, String> outgoingHeaders,
                                String outgoingBodyType,
                                byte[] outgoingBody,
                                HttpResponse response,
                                long emulatedLoadTime) {
        this.method = method;
        this.url = url;
        this.outgoingHeaders = outgoingHeaders;
        this.outgoingBodyType = outgoingBodyType;
        this.outgoingBody = outgoingBody;
        this.response = response;
        this.emulatedLoadTime = emulatedLoadTime;
    }


    public HttpResponse provideResponse(@NonNull Request<?> request) throws IOException, AuthFailureError {
        if (!matchesRequest(request))
            throw new RequestMismatchException();

        try {
            Thread.sleep(emulatedLoadTime);
        } catch (InterruptedException e) {
            Log.w(getClass().getSimpleName(), "Emulated load time interrupted", e);
        }

        return response;
    }


    //region Identity

    private static boolean equalObjects(Object a, Object b) {
        return (a == b) || (a != null && b != null && a.equals(b));
    }

    public boolean matchesRequest(@NonNull Request<?> request) {
        try {
            return (method == request.getMethod() &&
                    equalObjects(url, request.getUrl()) &&
                    equalObjects(outgoingHeaders, request.getHeaders()) &&
                    equalObjects(outgoingBody, request.getBody()));
        } catch (AuthFailureError e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "StubResponseProvider{" +
                "method=" + method +
                ", url='" + url + '\'' +
                ", outgoingHeaders=" + outgoingHeaders +
                ", outgoingBodyType='" + outgoingBodyType + '\'' +
                ", outgoingBody=" + Arrays.toString(outgoingBody) +
                ", response=" + response +
                '}';
    }

    //endregion


    public static class RequestMismatchException extends IOException {
        public RequestMismatchException() {
            super("Stub response provider invoked with unexpected request");
        }
    }


    public static class Builder {
        StubHttpStack stack;

        int method;
        String url;
        Map<String, String> outgoingHeaders;
        String outgoingBodyType;
        byte[] outgoingBody;

        HttpResponse response;
        long emulatedLoadTime;


        public Builder(StubHttpStack stack) {
            this.stack = stack;
            this.method = Request.Method.GET;
            this.emulatedLoadTime = 200;
        }


        //region Setters

        public Builder setMethod(int method) {
            this.method = method;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setOutgoingHeaders(Map<String, String> outgoingHeaders) {
            this.outgoingHeaders = outgoingHeaders;
            return this;
        }

        public Builder setOutgoingBodyType(String outgoingBodyType) {
            this.outgoingBodyType = outgoingBodyType;
            return this;
        }

        public Builder setOutgoingBody(byte[] outgoingBody) {
            this.outgoingBody = outgoingBody;
            return this;
        }

        public Builder setResponse(HttpResponse response) {
            this.response = response;
            return this;
        }

        public Builder setEmulatedLoadTime(long emulatedLoadTime) {
            this.emulatedLoadTime = emulatedLoadTime;
            return this;
        }

        //endregion


        //region Convenience Stubbing

        public void andReturnEntity(@NonNull HttpEntity entity,
                                    @NonNull Map<String, String> headers,
                                    int statusCode) {
            HttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, "OK"));
            for (Map.Entry<String, String> header : headers.entrySet())
                response.setHeader(header.getKey(), header.getValue());
            response.setEntity(entity);
            setResponse(response);
            add();
        }

        public void andReturnStream(@NonNull InputStream stream,
                                    int streamLength,
                                    @NonNull Map<String, String> headers,
                                    int statusCode) {
            andReturnEntity(new InputStreamEntity(stream, streamLength), headers, statusCode);
        }

        public void andReturnString(@NonNull String string,
                                    @NonNull Map<String, String> headers,
                                    int statusCode) {
            try {
                andReturnEntity(new StringEntity(string), headers, statusCode);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        public void andReturnJson(@NonNull JSONObject object,
                                  @NonNull Map<String, String> headers,
                                  int statusCode) {
            headers.put("Content-Type", "application/json");
            andReturnString(object.toString(), headers, statusCode);
        }

        public void andReturnJson(@Nullable Object object,
                                  @NonNull JsonConverter converter,
                                  @NonNull Map<String, String> headers,
                                  int statusCode) {
            try {
                headers.put("Content-Type", "application/json");
                andReturnString(converter.convertToJsonString(object), headers, statusCode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //endregion


        public StubResponseProvider build() {
            return new StubResponseProvider(method, url, outgoingHeaders, outgoingBodyType, outgoingBody, response, emulatedLoadTime);
        }

        public void add() {
            stack.addStub(build());
        }
    }
}
