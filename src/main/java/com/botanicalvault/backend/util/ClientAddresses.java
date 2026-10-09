package com.botanicalvault.backend.util;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientAddresses {
    private static final String FORWARDED_FOR = "X-Forwarded-For";
    private static final String HOP_SEPARATOR = ",";

    private ClientAddresses() {
    }

    public static String of(HttpServletRequest request, boolean trustForwardedFor) {
        String forwardedFor = request.getHeader(FORWARDED_FOR);
        if (!trustForwardedFor || forwardedFor == null) {
            return request.getRemoteAddr();
        }
        return Arrays.stream(forwardedFor.split(HOP_SEPARATOR))
                .map(String::trim)
                .filter(hop -> !hop.isEmpty())
                .reduce((first, second) -> second)
                .orElse(request.getRemoteAddr());
    }
}
