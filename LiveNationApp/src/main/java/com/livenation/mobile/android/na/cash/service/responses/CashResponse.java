package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CashResponse {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
