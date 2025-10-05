package com.example.backend.util;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    
    private static final String SUCCESS = "success";
    private static final String MESSAGE = "message";

    private ApiResponse() {}
    
    public static Map<String, Object> success(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MESSAGE, message);
        return response;
    }
    
    public static Map<String, Object> success(String message, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);
        response.put(MESSAGE, message);
        response.putAll(data);
        return response;
    }
    
    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, message);
        return response;
    }
    
    public static Map<String, Object> error(String message, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, false);
        response.put(MESSAGE, message);
        response.putAll(data);
        return response;
    }
    
    public static class Builder {
        private final Map<String, Object> response = new HashMap<>();
        private boolean isSuccess = true;
        
        public Builder success() {
            this.isSuccess = true;
            return this;
        }
        
        public Builder error() {
            this.isSuccess = false;
            return this;
        }
        
        public Builder message(String message) {
            response.put(MESSAGE, message);
            return this;
        }
        
        public Builder data(String key, Object value) {
            response.put(key, value);
            return this;
        }
        
        public Builder data(Map<String, Object> data) {
            response.putAll(data);
            return this;
        }
        
        public Map<String, Object> build() {
            response.put(SUCCESS, isSuccess);
            return response;
        }
    }
    
    public static Builder successBuilder() {
        return new Builder().success();
    }
    
    public static Builder errorBuilder() {
        return new Builder().error();
    }
}