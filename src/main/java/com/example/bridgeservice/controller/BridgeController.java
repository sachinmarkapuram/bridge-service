package com.example.bridgeservice.controller;

import com.example.bridgeservice.model.BridgeRequest;
import com.example.bridgeservice.model.BridgeResponse;
import com.example.bridgeservice.service.BridgeOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for bridge operations
 */
@RestController
@RequestMapping("/api/bridge")
@CrossOrigin(origins = "*")
public class BridgeController {

    private static final Logger logger = LoggerFactory.getLogger(BridgeController.class);

    @Autowired
    private BridgeOrchestrationService orchestrationService;

    /**
     * Route request to a specific application
     * POST /api/bridge/route
     */
    @PostMapping("/route")
    public ResponseEntity<BridgeResponse> routeRequest(@RequestBody BridgeRequest request) {
        logger.info("Received route request: {}", request);
        
        try {
            BridgeResponse response = orchestrationService.routeToApplication(request);
            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            logger.error("Error processing route request: {}", e.getMessage(), e);
            BridgeResponse errorResponse = BridgeResponse.error("Internal server error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Coordinate data flow from Application A to Application B
     * POST /api/bridge/coordinate/a-to-b
     */
    @PostMapping("/coordinate/a-to-b")
    public ResponseEntity<BridgeResponse> coordinateAToB(@RequestBody BridgeRequest request) {
        logger.info("Received A->B coordination request: {}", request);
        
        try {
            BridgeResponse response = orchestrationService.coordinateAToB(request);
            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            logger.error("Error processing A->B coordination: {}", e.getMessage(), e);
            BridgeResponse errorResponse = BridgeResponse.error("Internal server error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Coordinate data flow from Application B to Application A
     * POST /api/bridge/coordinate/b-to-a
     */
    @PostMapping("/coordinate/b-to-a")
    public ResponseEntity<BridgeResponse> coordinateBToA(@RequestBody BridgeRequest request) {
        logger.info("Received B->A coordination request: {}", request);
        
        try {
            BridgeResponse response = orchestrationService.coordinateBToA(request);
            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            logger.error("Error processing B->A coordination: {}", e.getMessage(), e);
            BridgeResponse errorResponse = BridgeResponse.error("Internal server error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Parallel coordination - send data to both applications simultaneously
     * POST /api/bridge/coordinate/parallel
     */
    @PostMapping("/coordinate/parallel")
    public ResponseEntity<BridgeResponse> coordinateParallel(@RequestBody BridgeRequest request) {
        logger.info("Received parallel coordination request: {}", request);
        
        try {
            BridgeResponse response = orchestrationService.coordinateParallel(request);
            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            logger.error("Error processing parallel coordination: {}", e.getMessage(), e);
            BridgeResponse errorResponse = BridgeResponse.error("Internal server error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Health check endpoint
     * GET /api/bridge/health
     */
    @GetMapping("/health")
    public ResponseEntity<BridgeResponse> health() {
        logger.info("Received health check request");
        
        try {
            BridgeResponse response = orchestrationService.healthCheck();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error processing health check: {}", e.getMessage(), e);
            BridgeResponse errorResponse = BridgeResponse.error("Health check failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get bridge service information
     * GET /api/bridge/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        logger.info("Received info request");
        
        Map<String, Object> info = Map.of(
            "serviceName", "Bridge Service",
            "version", "1.0.0",
            "description", "Service for connecting two stateless applications",
            "endpoints", Map.of(
                "route", "POST /api/bridge/route",
                "coordinateAToB", "POST /api/bridge/coordinate/a-to-b",
                "coordinateBToA", "POST /api/bridge/coordinate/b-to-a",
                "coordinateParallel", "POST /api/bridge/coordinate/parallel",
                "health", "GET /api/bridge/health",
                "info", "GET /api/bridge/info"
            ),
            "supportedOperations", Map.of(
                "routing", "Route requests to specific applications",
                "coordination", "Coordinate data flow between applications",
                "parallel", "Execute operations in parallel on both applications",
                "monitoring", "Health check and service information"
            )
        );
        
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    /**
     * Simple ping endpoint for basic health check
     * GET /api/bridge/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return new ResponseEntity<>(Map.of("status", "pong"), HttpStatus.OK);
    }
}
