package com.botanicalvault.backend.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientAddressesTest {
    private static final String REMOTE = "169.254.1.1";

    @Test
    void usesTheLastForwardedHopWhenTrusted() {
        assertThat(ClientAddresses.of(request("1.2.3.4, 203.0.113.7"), true)).isEqualTo("203.0.113.7");
    }

    @Test
    void ignoresForwardedHeaderWhenNotTrusted() {
        assertThat(ClientAddresses.of(request("203.0.113.7"), false)).isEqualTo(REMOTE);
    }

    @Test
    void fallsBackToRemoteAddressWithoutUsableHeader() {
        assertThat(ClientAddresses.of(request(null), true)).isEqualTo(REMOTE);
        assertThat(ClientAddresses.of(request(" , "), true)).isEqualTo(REMOTE);
    }

    private static MockHttpServletRequest request(String forwardedFor) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(REMOTE);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        return request;
    }
}
