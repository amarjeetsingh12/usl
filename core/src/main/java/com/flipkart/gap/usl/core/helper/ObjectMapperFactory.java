package com.flipkart.gap.usl.core.helper;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by amarjeet.singh on 05/10/16.
 */
public class ObjectMapperFactory {
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static <L> L deepCopy(L object) throws IOException {
        return getMapper().readValue(getMapper().writeValueAsBytes(object), (Class<L>) object.getClass());
    }

    public static <T> T listFromJSON(final TypeReference<T> type, final String jsonPacket) throws IOException {
        T data = getMapper().readValue(jsonPacket, type);
        return data;
    }

    public static <T> T fromByteArray(final Class<T> type, byte[] bytes) throws IOException {
        return getMapper().readValue(bytes, type);
    }
}
