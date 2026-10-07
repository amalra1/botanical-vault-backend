package com.botanicalvault.backend.util;

import java.util.Locale;

public final class EmailAddresses {

    private EmailAddresses() {
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
