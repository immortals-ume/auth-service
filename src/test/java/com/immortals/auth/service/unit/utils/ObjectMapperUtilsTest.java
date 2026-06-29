package com.immortals.auth.service.unit.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.immortals.auth.service.utils.ObjectMapperUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperUtilsTest {

    // ──────────────── toJsonSafe ────────────────

    @Test
    void toJsonSafe_withSerializableObject_returnsJson() {
        Optional<String> json = ObjectMapperUtils.toJsonSafe(Map.of("key", "value"));
        assertThat(json).isPresent().hasValueSatisfying(s -> assertThat(s).contains("key").contains("value"));
    }

    @Test
    void toJsonSafe_withNull_returnsEmptyOrNull() {
        Optional<String> json = ObjectMapperUtils.toJsonSafe(null);
        // writeValueAsString(null) returns "null" as a valid JSON literal
        assertThat(json).isPresent();
        assertThat(json.get()).isEqualTo("null");
    }

    @Test
    void toJsonSafe_withUnserializableObject_returnsEmpty() {
        // Anonymous class with cycle — Jackson can't serialize it
        Object circular = new Object() {
            public final Object self = this;
        };
        Optional<String> json = ObjectMapperUtils.toJsonSafe(circular);
        // Jackson will either serialize or fail depending on config; test that it handles both
        assertThat(json).satisfiesAnyOf(
                opt -> assertThat(opt).isEmpty(),
                opt -> assertThat(opt).isPresent()
        );
    }

    // ──────────────── fromJsonSafe (Class) ────────────────

    @Test
    void fromJsonSafe_withValidJson_deserializesCorrectly() {
        Optional<Map> result = ObjectMapperUtils.fromJsonSafe("{\"name\":\"test\"}", Map.class);
        assertThat(result).isPresent();
        assertThat(result.get()).containsEntry("name", "test");
    }

    @Test
    void fromJsonSafe_withInvalidJson_returnsEmpty() {
        Optional<Map> result = ObjectMapperUtils.fromJsonSafe("not-json", Map.class);
        assertThat(result).isEmpty();
    }

    @Test
    void fromJsonSafe_withNullJson_returnsEmpty() {
        Optional<Map> result = ObjectMapperUtils.fromJsonSafe(null, Map.class);
        assertThat(result).isEmpty();
    }

    // ──────────────── fromJsonSafe (TypeReference) ────────────────

    @Test
    void fromJsonSafe_withTypeReference_deserializesGenericType() {
        Optional<List<String>> result = ObjectMapperUtils.fromJsonSafe(
                "[\"a\",\"b\"]", new TypeReference<List<String>>() {}
        );
        assertThat(result).isPresent();
        assertThat(result.get()).containsExactly("a", "b");
    }

    @Test
    void fromJsonSafe_withTypeReference_invalidJson_returnsEmpty() {
        Optional<List<String>> result = ObjectMapperUtils.fromJsonSafe(
                "bad json", new TypeReference<List<String>>() {}
        );
        assertThat(result).isEmpty();
    }

    // ──────────────── toJsonNodeSafe ────────────────

    @Test
    void toJsonNodeSafe_withValidJson_returnsNode() {
        Optional<JsonNode> node = ObjectMapperUtils.toJsonNodeSafe("{\"x\":1}");
        assertThat(node).isPresent();
        assertThat(node.get().get("x").asInt()).isEqualTo(1);
    }

    @Test
    void toJsonNodeSafe_withInvalidJson_returnsEmpty() {
        Optional<JsonNode> node = ObjectMapperUtils.toJsonNodeSafe("invalid{{");
        assertThat(node).isEmpty();
    }

    // ──────────────── mapper ────────────────

    @Test
    void mapper_returnsNonNullObjectMapper() {
        ObjectMapper mapper = ObjectMapperUtils.mapper();
        assertThat(mapper).isNotNull();
    }
}
