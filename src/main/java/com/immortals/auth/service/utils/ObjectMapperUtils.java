package com.immortals.auth.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Optional;

public final class ObjectMapperUtils {

    private static final ObjectMapper MAPPER = buildMapper();

    private ObjectMapperUtils() {
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    public static Optional<String> toJsonSafe(Object object) {
        try {
            return Optional.ofNullable(MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public static <T> Optional<T> fromJsonSafe(String json, Class<T> clazz) {
        try {
            return Optional.ofNullable(MAPPER.readValue(json, clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public static <T> Optional<T> fromJsonSafe(String json, TypeReference<T> typeRef) {
        try {
            return Optional.ofNullable(MAPPER.readValue(json, typeRef));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public static Optional<JsonNode> toJsonNodeSafe(String json) {
        try {
            return Optional.ofNullable(MAPPER.readTree(json));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}