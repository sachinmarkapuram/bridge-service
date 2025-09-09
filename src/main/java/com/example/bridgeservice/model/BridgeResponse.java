package com.example.bridgeservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response model for bridge operations
 */
public class BridgeResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("sourceApplication")
    private String sourceApplication;

    // Default constructor
    public BridgeResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for success response
    public BridgeResponse(boolean success, Map<String, Object> data, String message) {
        this();
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Static factory methods for convenience
    public static BridgeResponse success(Map<String, Object> data, String message) {
        return new BridgeResponse(true, data, message);
    }

    public static BridgeResponse error(String message) {
        return new BridgeResponse(false, null, message);
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    @Override
    public String toString() {
        return "BridgeResponse{" +
                "success=" + success +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", timestamp=" + timestamp +
                ", sourceApplication='" + sourceApplication + '\'' +
                '}';
    }
}
