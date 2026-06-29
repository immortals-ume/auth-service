package com.immortals.auth.service.unit.context;

import com.immortals.auth.service.context.RequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContextTest {

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    @Test
    void setAndGetUserAgent_returnsSetValue() {
        RequestContext.setUserAgent("Mozilla/5.0");
        assertThat(RequestContext.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    void getUserAgent_whenNotSet_returnsNull() {
        assertThat(RequestContext.getUserAgent()).isNull();
    }

    @Test
    void clear_removesUserAgent() {
        RequestContext.setUserAgent("Mozilla/5.0");
        RequestContext.clear();
        assertThat(RequestContext.getUserAgent()).isNull();
    }

    @Test
    void setUserAgent_overwritesPreviousValue() {
        RequestContext.setUserAgent("AgentA");
        RequestContext.setUserAgent("AgentB");
        assertThat(RequestContext.getUserAgent()).isEqualTo("AgentB");
    }
}
