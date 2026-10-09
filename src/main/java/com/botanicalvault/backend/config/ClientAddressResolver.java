package com.botanicalvault.backend.config;

import com.botanicalvault.backend.util.ClientAddresses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientAddressResolver {
    private final LoginThrottleProperties properties;

    public String resolve(HttpServletRequest request) {
        return ClientAddresses.of(request, properties.trustForwardedFor());
    }
}
