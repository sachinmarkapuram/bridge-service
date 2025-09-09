package com.example.bridgeservice.client;

import com.example.bridgeservice.config.ApplicationProperties;
import com.example.bridgeservice.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client service for communicating with Application B
 */
@Service
public class ApplicationBClient {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationBClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ApplicationProperties properties;

    /**
     * Send data to Application B
     */
    @CircuitBreaker(name = "applicationB", fallbackMethod = "fallbackSendData")
    @Retry(name = "applicationB")
    public Map<String, Object> sendData(Map<String, Object> data, String operation) {
        if (!properties.getApplicationB().isEnabled()) {
            throw new ServiceUnavailableException("Application B is disabled");
        }

        String url = properties.getApplicationB().getBaseUrl() + "/api/" + operation;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Add API key if configured
        if (properties.getApplicationB().getApiKey() != null) {
            headers.set("X-API-Key", properties.getApplicationB().getApiKey());
        }
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        
        logger.info("Sending request to Application B: {} with data: {}", url, data);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            logger.info("Received response from Application B: {}", response.getBody());
            return (Map<String, Object>) response.getBody();
            
        } catch (Exception e) {
            logger.error("Error communicating with Application B: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to communicate with Application B: " + e.getMessage());
        }
    }

    /**
     * Get data from Application B
     */
    @CircuitBreaker(name = "applicationB", fallbackMethod = "fallbackGetData")
    @Retry(name = "applicationB")
    public Map<String, Object> getData(String endpoint, Map<String, String> params) {
        if (!properties.getApplicationB().isEnabled()) {
            throw new ServiceUnavailableException("Application B is disabled");
        }

        String url = properties.getApplicationB().getBaseUrl() + "/api/" + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        if (properties.getApplicationB().getApiKey() != null) {
            headers.set("X-API-Key", properties.getApplicationB().getApiKey());
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        logger.info("Getting data from Application B: {}", url);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                Map.class
            );
            
            logger.info("Received data from Application B: {}", response.getBody());
            return (Map<String, Object>) response.getBody();
            
        } catch (Exception e) {
            logger.error("Error getting data from Application B: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to get data from Application B: " + e.getMessage());
        }
    }

    /**
     * Process data with Application B (specific business logic)
     */
    @CircuitBreaker(name = "applicationB", fallbackMethod = "fallbackProcessData")
    @Retry(name = "applicationB")
    public Map<String, Object> processData(Map<String, Object> data, String processingType) {
        if (!properties.getApplicationB().isEnabled()) {
            throw new ServiceUnavailableException("Application B is disabled");
        }

        String url = properties.getApplicationB().getBaseUrl() + "/api/process/" + processingType;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (properties.getApplicationB().getApiKey() != null) {
            headers.set("X-API-Key", properties.getApplicationB().getApiKey());
        }
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        
        logger.info("Processing data with Application B: {} with data: {}", url, data);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            logger.info("Received processed data from Application B: {}", response.getBody());
            return (Map<String, Object>) response.getBody();
            
        } catch (Exception e) {
            logger.error("Error processing data with Application B: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to process data with Application B: " + e.getMessage());
        }
    }

    /**
     * Fallback method for sendData
     */
    public Map<String, Object> fallbackSendData(Map<String, Object> data, String operation, Exception ex) {
        logger.warn("Using fallback for Application B sendData due to: {}", ex.getMessage());
        return Map.of(
            "success", false,
            "message", "Application B is currently unavailable",
            "fallback", true
        );
    }

    /**
     * Fallback method for getData
     */
    public Map<String, Object> fallbackGetData(String endpoint, Map<String, String> params, Exception ex) {
        logger.warn("Using fallback for Application B getData due to: {}", ex.getMessage());
        return Map.of(
            "success", false,
            "message", "Application B is currently unavailable",
            "fallback", true
        );
    }

    /**
     * Fallback method for processData
     */
    public Map<String, Object> fallbackProcessData(Map<String, Object> data, String processingType, Exception ex) {
        logger.warn("Using fallback for Application B processData due to: {}", ex.getMessage());
        return Map.of(
            "success", false,
            "message", "Application B processing is currently unavailable",
            "fallback", true
        );
    }
}
