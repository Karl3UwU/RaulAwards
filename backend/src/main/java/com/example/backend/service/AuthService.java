package com.example.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_FRIEND = "FRIEND";

    private static class UserRecord {
        final String username;
        final String role;
        UserRecord(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }

    // Environment-provided configuration
    @Value("${AUTH_FRIEND_HASH:}")
    private String friendHashBase64;

    @Value("${AUTH_ADMIN_HASH:}")
    private String adminHashBase64;

    // A single salt configured in environment; can be rotated and differs across envs
    @Value("${AUTH_SALT:}")
    private String saltBase64;

    // PBKDF2 iterations; configurable via env, defaults to 100_000
    @Value("${AUTH_ITERATIONS:100000}")
    private int iterations;

    private final Map<String, UserRecord> tokenStore = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        // Ensure configuration is present
        if (isBlank(friendHashBase64) || isBlank(adminHashBase64) || isBlank(saltBase64)) {
            return null;
        }

        String expectedHash;
        String role;
        if ("friend".equals(username)) {
            expectedHash = friendHashBase64;
            role = ROLE_FRIEND;
        } else if ("admin".equals(username)) {
            expectedHash = adminHashBase64;
            role = ROLE_ADMIN;
        } else {
            return null;
        }

        byte[] salt = decodeBase64(saltBase64);
        String calculated = hashPassword(password, salt, iterations);
        if (!constantTimeEquals(expectedHash, calculated)) {
            return null;
        }

        String token = UUID.randomUUID().toString();
        tokenStore.put(token, new UserRecord(username, role));
        return token;
    }

    public boolean validate(String token) {
        return token != null && tokenStore.containsKey(token);
    }

    public String getRole(String token) {
        UserRecord record = tokenStore.get(token);
        return record == null ? null : record.role;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value);
    }

    private static String hashPassword(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int result = 0;
        for (int i = 0; i < x.length; i++) {
            result |= x[i] ^ y[i];
        }
        return result == 0;
    }
}


