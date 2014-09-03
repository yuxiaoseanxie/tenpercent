package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class CashResponse implements Serializable {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static <T extends CashResponse> T fromJsonString(String body, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(body, clazz);
    }

    public boolean validateForJsonConversion() {
        return true;
    }

    public String toJsonString() throws IOException {
        if (!validateForJsonConversion()) {
            throw new IOException("Json string validation failed");
        }

        return OBJECT_MAPPER.writeValueAsString(this);
    }
}