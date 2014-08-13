package stubs.converters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonJsonConverter implements JsonConverter {
    private final ObjectMapper objectMapper;

    public JacksonJsonConverter(@NonNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public String convertToJsonString(@Nullable Object object) throws Exception {
        return getObjectMapper().writeValueAsString(object);
    }


    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public String toString() {
        return "JacksonBodyConverter{" +
                "objectMapper=" + objectMapper +
                '}';
    }
}
