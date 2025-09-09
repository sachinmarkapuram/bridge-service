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

import java.time.Duration;
import java.util.Map;

/**
 * Client service for communicating with Application A
 */
@Service
public class ApplicationAClient {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationAClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ApplicationProperties properties;

    /**
     * Send data to Application A
     */
    @CircuitBreaker(name = "applicationA", fallbackMethod = "fallbackSendData")
    @Retry(name = "applicationA")
    public Map<String, Object> sendData(Map<String, Object> data, String operation) {
        if (!properties.getApplicationA().isEnabled()) {
            throw new ServiceUnavailableException("Application A is disabled");
        }

        String url = properties.getApplicationA().getBaseUrl() + "/api/" + operation;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Add API key if configured
        if (properties.getApplicationA().getApiKey() != null) {
            headers.set("X-API-Key", properties.getApplicationA().getApiKey());
        }
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        
        logger.info("Sending request to Application A: {} with data: {}", url, data);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            logger.info("Received response from Application A: {}", response.getBody());
            return (Map<String, Object>) response.getBody();
            
        } catch (Exception e) {
            logger.error("Error communicating with Application A: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to communicate with Application A: " + e.getMessage());
        }
    }

    /**
     * Get data from Application A
     */
    @CircuitBreaker(name = "applicationA", fallbackMethod = "fallbackGetData")
    @Retry(name = "applicationA")
    public Map<String, Object> getData(String endpoint, Map<String, String> params) {
        if (!properties.getApplicationA().isEnabled()) {
            throw new ServiceUnavailableException("Application A is disabled");
        }

        String url = properties.getApplicationA().getBaseUrl() + "/api/" + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        if (properties.getApplicationA().getApiKey() != null) {
            headers.set("X-API-Key", properties.getApplicationA().getApiKey());
        }
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        logger.info("Getting data from Application A: {}", url);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                Map.class
            );
            
            logger.info("Received data from Application A: {}", response.getBody());
            return (Map<String, Object>) response.getBody();
            
        } catch (Exception e) {
            logger.error("Error getting data from Application A: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to get data from Application A: " + e.getMessage());
        }
    }

    /**
     * Fallback method for sendData
     */
    public Map<String, Object> fallbackSendData(Map<String, Object> data, String operation, Exception ex) {
        logger.warn("Using fallback for Application A sendData due to: {}", ex.getMessage());
        return Map.of(
            "success", false,
            "message", "Application A is currently unavailable",
            "fallback", true
        );
    }

    /**
     * Fallback method for getData
     */
    public Map<String, Object> fallbackGetData(String endpoint, Map<String, String> params, Exception ex) {
        logger.warn("Using fallback for Application A getData due to: {}", ex.getMessage());
        return Map.of(
            "success", false,
            "message", "Application A is currently unavailable",
            "fallback", true
        );
    }
}
