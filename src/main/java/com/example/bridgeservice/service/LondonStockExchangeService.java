package com.example.bridgeservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling asynchronous calls to London Stock Exchange Open API
 */
@Service
public class LondonStockExchangeService {

    private static final Logger logger = LoggerFactory.getLogger(LondonStockExchangeService.class);
    
    // LSE Open API base URL (using a public endpoint for demonstration)
    private static final String LSE_API_BASE_URL = "https://api.londonstockexchange.com/api/v1";
    
    private final WebClient webClient;

    @Autowired
    public LondonStockExchangeService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl(LSE_API_BASE_URL)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }

    /**
     * Asynchronously fetch market data from London Stock Exchange
     * @param correlationId Correlation ID for tracking the request
     * @return CompletableFuture containing the market data
     */
    @Async
    public CompletableFuture<Map<String, Object>> fetchMarketDataAsync(String correlationId) {
        logger.info("Starting async fetch of LSE market data with correlation ID: {}", correlationId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchMarketData(correlationId);
            } catch (Exception e) {
                logger.error("Error in async market data fetch for correlation ID {}: {}", correlationId, e.getMessage(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", "Failed to fetch market data: " + e.getMessage());
                errorResult.put("correlationId", correlationId);
                return errorResult;
            }
        });
    }

    /**
     * Asynchronously fetch specific stock data from London Stock Exchange
     * @param symbol Stock symbol to fetch
     * @param correlationId Correlation ID for tracking the request
     * @return CompletableFuture containing the stock data
     */
    @Async
    public CompletableFuture<Map<String, Object>> fetchStockDataAsync(String symbol, String correlationId) {
        logger.info("Starting async fetch of LSE stock data for symbol: {} with correlation ID: {}", symbol, correlationId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetchStockData(symbol, correlationId);
            } catch (Exception e) {
                logger.error("Error in async stock data fetch for symbol {} with correlation ID {}: {}", symbol, correlationId, e.getMessage(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("error", "Failed to fetch stock data for " + symbol + ": " + e.getMessage());
                errorResult.put("correlationId", correlationId);
                errorResult.put("symbol", symbol);
                return errorResult;
            }
        });
    }

    /**
     * Fetch general market data from LSE
     */
    private Map<String, Object> fetchMarketData(String correlationId) {
        try {
            logger.info("Fetching market data from LSE API for correlation ID: {}", correlationId);
            
            // For demonstration, we'll create a mock response since we don't have actual LSE API credentials
            // In a real implementation, you would uncomment and modify the WebClient call below
            
            /*
            Map<String, Object> response = webClient
                .get()
                .uri("/market/data")
                .header("Authorization", "Bearer YOUR_API_KEY")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            */
            
            // Mock response for demonstration
            Map<String, Object> mockResponse = createMockMarketData(correlationId);
            
            logger.info("Successfully fetched market data for correlation ID: {}", correlationId);
            return mockResponse;
            
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching market data for correlation ID {}: {} - {}", correlationId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("LSE API returned error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error fetching market data for correlation ID {}: {}", correlationId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch market data: " + e.getMessage());
        }
    }

    /**
     * Fetch specific stock data from LSE
     */
    private Map<String, Object> fetchStockData(String symbol, String correlationId) {
        try {
            logger.info("Fetching stock data for symbol: {} with correlation ID: {}", symbol, correlationId);
            
            // For demonstration, we'll create a mock response since we don't have actual LSE API credentials
            // In a real implementation, you would uncomment and modify the WebClient call below
            
            /*
            Map<String, Object> response = webClient
                .get()
                .uri("/stocks/{symbol}", symbol)
                .header("Authorization", "Bearer YOUR_API_KEY")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();
            */
            
            // Mock response for demonstration
            Map<String, Object> mockResponse = createMockStockData(symbol, correlationId);
            
            logger.info("Successfully fetched stock data for symbol: {} with correlation ID: {}", symbol, correlationId);
            return mockResponse;
            
        } catch (WebClientResponseException e) {
            logger.error("HTTP error fetching stock data for symbol {} with correlation ID {}: {} - {}", symbol, correlationId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("LSE API returned error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error fetching stock data for symbol {} with correlation ID {}: {}", symbol, correlationId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch stock data for " + symbol + ": " + e.getMessage());
        }
    }

    /**
     * Create mock market data for demonstration purposes
     */
    private Map<String, Object> createMockMarketData(String correlationId) {
        Map<String, Object> marketData = new HashMap<>();
        marketData.put("success", true);
        marketData.put("correlationId", correlationId);
        marketData.put("timestamp", System.currentTimeMillis());
        marketData.put("source", "London Stock Exchange");
        marketData.put("dataType", "market_overview");
        
        Map<String, Object> data = new HashMap<>();
        data.put("ftse100", 7423.45);
        data.put("ftse250", 18234.67);
        data.put("ftseAll", 4123.89);
        data.put("volume", 1234567890L);
        data.put("trades", 45678);
        data.put("marketStatus", "OPEN");
        data.put("lastUpdated", "2024-09-16T20:19:11Z");
        
        marketData.put("data", data);
        
        return marketData;
    }

    /**
     * Create mock stock data for demonstration purposes
     */
    private Map<String, Object> createMockStockData(String symbol, String correlationId) {
        Map<String, Object> stockData = new HashMap<>();
        stockData.put("success", true);
        stockData.put("correlationId", correlationId);
        stockData.put("timestamp", System.currentTimeMillis());
        stockData.put("source", "London Stock Exchange");
        stockData.put("dataType", "stock_data");
        stockData.put("symbol", symbol);
        
        Map<String, Object> data = new HashMap<>();
        data.put("price", 125.67 + (Math.random() * 20 - 10)); // Random price variation
        data.put("change", (Math.random() * 4 - 2)); // Random change
        data.put("changePercent", (Math.random() * 3 - 1.5)); // Random percentage change
        data.put("volume", (long)(Math.random() * 1000000));
        data.put("high", 130.45 + (Math.random() * 5));
        data.put("low", 120.23 + (Math.random() * 5));
        data.put("open", 123.45 + (Math.random() * 5));
        data.put("previousClose", 124.56 + (Math.random() * 5));
        data.put("marketCap", (long)(Math.random() * 50000000000L));
        data.put("lastUpdated", "2024-09-16T20:19:11Z");
        
        stockData.put("data", data);
        
        return stockData;
    }

    /**
     * Validate if the response from LSE API is successful
     */
    public boolean isValidResponse(Map<String, Object> response) {
        return response != null && 
               Boolean.TRUE.equals(response.get("success")) &&
               response.containsKey("data");
    }
}
