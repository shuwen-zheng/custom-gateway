package com.shuwen.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJsonStr(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T parseObject(String jsonStr, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(jsonStr, clazz);
    }
}
