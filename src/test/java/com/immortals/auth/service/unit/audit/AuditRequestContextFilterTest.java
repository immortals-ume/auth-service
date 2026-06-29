package com.immortals.auth.service.unit.audit;

import com.immortals.auth.service.audit.AuditRequestContext;
import com.immortals.auth.service.audit.AuditRequestContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.immortals.auth.service.constant.AuthConstants.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditRequestContextFilterTest {

    private final AuditRequestContextFilter filter = new AuditRequestContextFilter();

    @Test
    void doFilter_withoutCorrelationId_generatesRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        String correlationId = response.getHeader(CORRELATION_ID_HEADER);
        assertThat(correlationId).isNotBlank();
    }

    @Test
    void doFilter_withExistingCorrelationId_propagatesIt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CORRELATION_ID_HEADER, "existing-corr-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isEqualTo("existing-corr-id");
    }

    @Test
    void doFilter_withXForwardedForHeader_populatesContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
    }

    @Test
    void doFilter_withXRealIpHeader_populatesContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
    }

    @Test
    void doFilter_withProxyClientIpHeader_populatesContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Proxy-Client-IP", "10.0.0.3");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
    }

    @Test
    void doFilter_withWlProxyClientIpHeader_populatesContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("WL-Proxy-Client-IP", "10.0.0.4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
    }

    @Test
    void doFilter_clearsContextAfterCompletion() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(AuditRequestContext.current()).isNull();
    }

    @Test
    void doFilter_withSessionActive_capturesSessionId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true);  // creates a session
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
    }

    @Test
    void doFilter_withUserAgentHeader_doesNotFail() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla/5.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CORRELATION_ID_HEADER)).isNotBlank();
    }
}
