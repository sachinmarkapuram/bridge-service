package com.example.bridgeservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Response model for London Stock Exchange API responses
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LSEResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("source")
    private String source;

    @JsonProperty("dataType")
    private String dataType;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    // Default constructor
    public LSEResponse() {
    }

    // Constructor for success response
    public LSEResponse(boolean success, String correlationId, String source, String dataType, Map<String, Object> data) {
        this.success = success;
        this.correlationId = correlationId;
        this.timestamp = System.currentTimeMillis();
        this.source = source;
        this.dataType = dataType;
        this.data = data;
    }

    // Constructor for error response
    public LSEResponse(boolean success, String correlationId, String error, String message) {
        this.success = success;
        this.correlationId = correlationId;
        this.timestamp = System.currentTimeMillis();
        this.error = error;
        this.message = message;
    }

    // Static factory methods
    public static LSEResponse success(String correlationId, String source, String dataType, Map<String, Object> data) {
        return new LSEResponse(true, correlationId, source, dataType, data);
    }

    public static LSEResponse error(String correlationId, String error, String message) {
        return new LSEResponse(false, correlationId, error, message);
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "LSEResponse{" +
                "success=" + success +
                ", correlationId='" + correlationId + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                ", dataType='" + dataType + '\'' +
                ", symbol='" + symbol + '\'' +
                ", data=" + data +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
