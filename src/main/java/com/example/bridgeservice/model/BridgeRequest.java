package com.example.bridgeservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Request model for bridge operations
 */
public class BridgeRequest {
    
    @JsonProperty("operation")
    private String operation;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    @JsonProperty("targetApplication")
    private String targetApplication;
    
    @JsonProperty("correlationId")
    private String correlationId;

    // Default constructor
    public BridgeRequest() {}

    // Constructor with parameters
    public BridgeRequest(String operation, Map<String, Object> data, String targetApplication) {
        this.operation = operation;
        this.data = data;
        this.targetApplication = targetApplication;
    }

    // Getters and setters
    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getTargetApplication() {
        return targetApplication;
    }

    public void setTargetApplication(String targetApplication) {
        this.targetApplication = targetApplication;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return "BridgeRequest{" +
                "operation='" + operation + '\'' +
                ", data=" + data +
                ", targetApplication='" + targetApplication + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}
