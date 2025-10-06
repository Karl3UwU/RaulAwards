package com.example.backend.util;

public final class AuthUtil {

    private AuthUtil() {}

    public static String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public static String extractToken(String authHeader, String tokenParam) {
        if (tokenParam != null && !tokenParam.isBlank()) {
            return tokenParam;
        }
        return extractToken(authHeader);
    }
}


