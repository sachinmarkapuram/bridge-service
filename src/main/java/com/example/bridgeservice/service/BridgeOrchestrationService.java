package com.example.bridgeservice.service;

import com.example.bridgeservice.client.ApplicationAClient;
import com.example.bridgeservice.client.ApplicationBClient;
import com.example.bridgeservice.model.BridgeRequest;
import com.example.bridgeservice.model.BridgeResponse;
import com.example.bridgeservice.service.LondonStockExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for orchestrating operations between Application A and Application B
 */
@Service
public class BridgeOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(BridgeOrchestrationService.class);

    @Autowired
    private ApplicationAClient applicationAClient;

    @Autowired
    private ApplicationBClient applicationBClient;

    @Autowired
    private LondonStockExchangeService londonStockExchangeService;

    /**
     * Route request to a specific application
     */
    public BridgeResponse routeToApplication(BridgeRequest request) {
        String correlationId = request.getCorrelationId() != null ? 
            request.getCorrelationId() : UUID.randomUUID().toString();

        logger.info("Processing bridge request with correlation ID: {}", correlationId);

        try {
            Map<String, Object> result;
            String sourceApp;

            switch (request.getTargetApplication().toLowerCase()) {
                case "a":
                case "application-a":
                    result = applicationAClient.sendData(request.getData(), request.getOperation());
                    sourceApp = "Application A";
                    break;
                case "b":
                case "application-b":
                    result = applicationBClient.sendData(request.getData(), request.getOperation());
                    sourceApp = "Application B";
                    break;
                default:
                    return createErrorResponse(correlationId, "Unknown target application: " + request.getTargetApplication());
            }

            BridgeResponse response = BridgeResponse.success(result, "Successfully routed to " + sourceApp);
            response.setCorrelationId(correlationId);
            response.setSourceApplication(sourceApp);
            
            return response;

        } catch (Exception e) {
            logger.error("Error routing request to {}: {}", request.getTargetApplication(), e.getMessage(), e);
            return createErrorResponse(correlationId, "Failed to route request: " + e.getMessage());
        }
    }

    /**
     * Coordinate data flow from Application A to Application B
     */
    public BridgeResponse coordinateAToB(BridgeRequest request) {
        String correlationId = request.getCorrelationId() != null ? 
            request.getCorrelationId() : UUID.randomUUID().toString();

        logger.info("Coordinating A->B flow with correlation ID: {}", correlationId);

        try {
            // Step 1: Get data from Application A
            Map<String, Object> dataFromA = applicationAClient.getData(
                request.getOperation(), 
                extractStringParams(request.getData())
            );

            if (dataFromA == null || !isSuccessfulResponse(dataFromA)) {
                return createErrorResponse(correlationId, "Failed to get data from Application A");
            }

            // Step 2: Process data with Application B
            Map<String, Object> processedData = applicationBClient.processData(
                dataFromA, 
                request.getOperation()
            );

            if (processedData == null || !isSuccessfulResponse(processedData)) {
                return createErrorResponse(correlationId, "Failed to process data with Application B");
            }

            // Step 3: Return coordinated result
            Map<String, Object> result = new HashMap<>();
            result.put("originalData", dataFromA);
            result.put("processedData", processedData);
            result.put("coordinatedBy", "Bridge Service");

            BridgeResponse response = BridgeResponse.success(result, "Successfully coordinated A->B flow");
            response.setCorrelationId(correlationId);
            response.setSourceApplication("Coordination: A->B");
            
            return response;

        } catch (Exception e) {
            logger.error("Error coordinating A->B flow: {}", e.getMessage(), e);
            return createErrorResponse(correlationId, "Failed to coordinate A->B flow: " + e.getMessage());
        }
    }

    /**
     * Coordinate data flow from Application B to Application A
     */
    public BridgeResponse coordinateBToA(BridgeRequest request) {
        String correlationId = request.getCorrelationId() != null ? 
            request.getCorrelationId() : UUID.randomUUID().toString();

        logger.info("Coordinating B->A flow with correlation ID: {}", correlationId);

        try {
            // Step 1: Get data from Application B
            Map<String, Object> dataFromB = applicationBClient.getData(
                request.getOperation(), 
                extractStringParams(request.getData())
            );

            if (dataFromB == null || !isSuccessfulResponse(dataFromB)) {
                return createErrorResponse(correlationId, "Failed to get data from Application B");
            }

            // Step 2: Send processed data to Application A
            Map<String, Object> resultFromA = applicationAClient.sendData(
                dataFromB, 
                request.getOperation()
            );

            if (resultFromA == null || !isSuccessfulResponse(resultFromA)) {
                return createErrorResponse(correlationId, "Failed to send data to Application A");
            }

            // Step 3: Return coordinated result
            Map<String, Object> result = new HashMap<>();
            result.put("originalData", dataFromB);
            result.put("resultFromA", resultFromA);
            result.put("coordinatedBy", "Bridge Service");

            BridgeResponse response = BridgeResponse.success(result, "Successfully coordinated B->A flow");
            response.setCorrelationId(correlationId);
            response.setSourceApplication("Coordination: B->A");
            
            return response;

        } catch (Exception e) {
            logger.error("Error coordinating B->A flow: {}", e.getMessage(), e);
            return createErrorResponse(correlationId, "Failed to coordinate B->A flow: " + e.getMessage());
        }
    }

    /**
     * Parallel coordination - send data to both applications simultaneously
     */
    public BridgeResponse coordinateParallel(BridgeRequest request) {
        String correlationId = request.getCorrelationId() != null ? 
            request.getCorrelationId() : UUID.randomUUID().toString();

        logger.info("Coordinating parallel operations with correlation ID: {}", correlationId);

        try {
            // Execute both operations in parallel
            CompletableFuture<Map<String, Object>> futureA = CompletableFuture.supplyAsync(() ->
                applicationAClient.sendData(request.getData(), request.getOperation())
            );

            CompletableFuture<Map<String, Object>> futureB = CompletableFuture.supplyAsync(() ->
                applicationBClient.sendData(request.getData(), request.getOperation())
            );

            // Wait for both to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futureA, futureB);
            allFutures.join();

            Map<String, Object> resultA = futureA.get();
            Map<String, Object> resultB = futureB.get();

            // Combine results
            Map<String, Object> result = new HashMap<>();
            result.put("applicationAResult", resultA);
            result.put("applicationBResult", resultB);
            result.put("executionType", "parallel");
            result.put("coordinatedBy", "Bridge Service");

            BridgeResponse response = BridgeResponse.success(result, "Successfully executed parallel coordination");
            response.setCorrelationId(correlationId);
            response.setSourceApplication("Coordination: Parallel");
            
            return response;

        } catch (Exception e) {
            logger.error("Error in parallel coordination: {}", e.getMessage(), e);
            return createErrorResponse(correlationId, "Failed to execute parallel coordination: " + e.getMessage());
        }
    }

    /**
     * Initiate asynchronous data fetch from London Stock Exchange
     */
    public BridgeResponse initiateLSEDataFetch(String dataType, String symbol) {
        String correlationId = UUID.randomUUID().toString();
        logger.info("Initiating LSE data fetch with correlation ID: {} for dataType: {} and symbol: {}", correlationId, dataType, symbol);

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("correlationId", correlationId);
            result.put("dataType", dataType);
            result.put("symbol", symbol);
            result.put("status", "initiated");
            result.put("message", "Async LSE data fetch has been initiated");
            result.put("timestamp", System.currentTimeMillis());

            // Trigger async fetch based on data type
            if ("market".equalsIgnoreCase(dataType)) {
                londonStockExchangeService.fetchMarketDataAsync(correlationId)
                    .thenAccept(lseData -> {
                        logger.info("LSE market data fetch completed for correlation ID: {}", correlationId);
                        // In a real application, you might store this data or notify consumers
                        // For now, we just log the successful completion
                    })
                    .exceptionally(throwable -> {
                        logger.error("LSE market data fetch failed for correlation ID {}: {}", correlationId, throwable.getMessage());
                        return null;
                    });
            } else if ("stock".equalsIgnoreCase(dataType) && symbol != null && !symbol.trim().isEmpty()) {
                londonStockExchangeService.fetchStockDataAsync(symbol, correlationId)
                    .thenAccept(lseData -> {
                        logger.info("LSE stock data fetch completed for symbol: {} with correlation ID: {}", symbol, correlationId);
                        // In a real application, you might store this data or notify consumers
                        // For now, we just log the successful completion
                    })
                    .exceptionally(throwable -> {
                        logger.error("LSE stock data fetch failed for symbol {} with correlation ID {}: {}", symbol, correlationId, throwable.getMessage());
                        return null;
                    });
            } else {
                return createErrorResponse(correlationId, "Invalid data type or missing symbol. Supported types: 'market' or 'stock' (with symbol)");
            }

            BridgeResponse response = BridgeResponse.success(result, "LSE data fetch initiated successfully");
            response.setCorrelationId(correlationId);
            response.setSourceApplication("London Stock Exchange Bridge");
            
            return response;

        } catch (Exception e) {
            logger.error("Error initiating LSE data fetch: {}", e.getMessage(), e);
            return createErrorResponse(UUID.randomUUID().toString(), "Failed to initiate LSE data fetch: " + e.getMessage());
        }
    }

    /**
     * Health check for both applications
     */
    public BridgeResponse healthCheck() {
        String correlationId = UUID.randomUUID().toString();
        logger.info("Performing health check with correlation ID: {}", correlationId);

        try {
            Map<String, Object> health = new HashMap<>();
            
            // Check Application A
            try {
                Map<String, Object> healthA = applicationAClient.getData("health", Map.of());
                health.put("applicationA", Map.of(
                    "status", "UP",
                    "response", healthA
                ));
            } catch (Exception e) {
                health.put("applicationA", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
                ));
            }

            // Check Application B
            try {
                Map<String, Object> healthB = applicationBClient.getData("health", Map.of());
                health.put("applicationB", Map.of(
                    "status", "UP",
                    "response", healthB
                ));
            } catch (Exception e) {
                health.put("applicationB", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
                ));
            }

            BridgeResponse response = BridgeResponse.success(health, "Health check completed");
            response.setCorrelationId(correlationId);
            response.setSourceApplication("Bridge Service Health Check");
            
            return response;

        } catch (Exception e) {
            logger.error("Error during health check: {}", e.getMessage(), e);
            return createErrorResponse(correlationId, "Health check failed: " + e.getMessage());
        }
    }

    private BridgeResponse createErrorResponse(String correlationId, String message) {
        BridgeResponse response = BridgeResponse.error(message);
        response.setCorrelationId(correlationId);
        return response;
    }

    private boolean isSuccessfulResponse(Map<String, Object> response) {
        return response != null && 
               !Boolean.FALSE.equals(response.get("success")) &&
               !Boolean.TRUE.equals(response.get("fallback"));
    }

    private Map<String, String> extractStringParams(Map<String, Object> data) {
        Map<String, String> params = new HashMap<>();
        if (data != null) {
            data.forEach((key, value) -> params.put(key, String.valueOf(value)));
        }
        return params;
    }
}
