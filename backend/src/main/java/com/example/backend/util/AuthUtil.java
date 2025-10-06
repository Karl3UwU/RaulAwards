package com.example.backend.util;

public final class AuthUtil {

    private AuthUtil() {}

    public static String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}


