# Bridge Service

A Java-based web application built with Spring Boot that acts as a bridge between two stateless applications. This service provides orchestration, routing, and coordination capabilities for inter-application communication.

## Features

- **Request Routing**: Route requests to specific applications (A or B)
- **Sequential Coordination**: Coordinate data flow from App A → App B or App B → App A
- **Parallel Execution**: Execute operations on both applications simultaneously
- **Circuit Breaker**: Built-in resilience with Resilience4j
- **Retry Logic**: Automatic retry with exponential backoff
- **Health Monitoring**: Health checks and monitoring endpoints
- **Fallback Handling**: Graceful degradation when services are unavailable

## Architecture

```
[Client] → [Bridge Service] → [Application A]
                ↓
         [Application B]
```

## API Endpoints

### Core Operations

- `POST /api/bridge/route` - Route request to a specific application
- `POST /api/bridge/coordinate/a-to-b` - Coordinate A→B data flow
- `POST /api/bridge/coordinate/b-to-a` - Coordinate B→A data flow  
- `POST /api/bridge/coordinate/parallel` - Execute parallel operations

### Monitoring

- `GET /api/bridge/health` - Comprehensive health check
- `GET /api/bridge/info` - Service information
- `GET /api/bridge/ping` - Simple ping/pong endpoint

### Management (Actuator)

- `GET /actuator/health` - Spring Boot health endpoint
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/circuitbreakers` - Circuit breaker status

## Request/Response Format

### Request Format

```json
{
  "operation": "process-data",
  "data": {
    "key1": "value1",
    "key2": "value2"
  },
  "targetApplication": "a",
  "correlationId": "optional-correlation-id"
}
```

### Response Format

```json
{
  "success": true,
  "data": {
    "result": "processed data"
  },
  "message": "Operation completed successfully",
  "correlationId": "correlation-id",
  "timestamp": "2023-10-01T12:00:00",
  "sourceApplication": "Application A"
}
```

## Configuration

The service can be configured via `application.yml`:

```yaml
bridge:
  services:
    application-a:
      base-url: http://localhost:8081
      api-key: your-api-key
      enabled: true
    application-b:
      base-url: http://localhost:8082  
      api-key: your-api-key
      enabled: true
    timeout-millis: 5000
    max-retries: 3
```

### Environment Variables

For production deployment:

- `APPLICATION_A_URL` - URL for Application A
- `APPLICATION_A_API_KEY` - API key for Application A
- `APPLICATION_B_URL` - URL for Application B
- `APPLICATION_B_API_KEY` - API key for Application B
- `SERVICE_TIMEOUT` - Request timeout in milliseconds
- `MAX_RETRIES` - Maximum retry attempts

## Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.6 or later

### Building

```bash
cd bridge-service
mvn clean compile
```

### Running

```bash
mvn spring-boot:run
```

Or run with a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=production
```

### Running with Docker

```bash
# Build the application
mvn clean package

# Create Docker image (optional - create Dockerfile)
docker build -t bridge-service .

# Run container
docker run -p 8080:8080 \
  -e APPLICATION_A_URL=http://app-a:8080 \
  -e APPLICATION_B_URL=http://app-b:8080 \
  bridge-service
```

## Usage Examples

### 1. Route to Application A

```bash
curl -X POST http://localhost:8080/api/bridge/route \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "process",
    "targetApplication": "a",
    "data": {"input": "test data"}
  }'
```

### 2. Coordinate A→B Flow

```bash
curl -X POST http://localhost:8080/api/bridge/coordinate/a-to-b \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "transform",
    "data": {"source": "application-a"}
  }'
```

### 3. Parallel Execution

```bash
curl -X POST http://localhost:8080/api/bridge/coordinate/parallel \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "sync",
    "data": {"timestamp": "2023-10-01T12:00:00Z"}
  }'
```

### 4. Health Check

```bash
curl http://localhost:8080/api/bridge/health
```

## Error Handling

The service provides comprehensive error handling:

- **Circuit Breaker**: Prevents cascade failures
- **Retry Logic**: Automatic retries with backoff
- **Fallback Responses**: Graceful degradation
- **Global Exception Handler**: Consistent error responses

## Monitoring and Observability

- **Health Checks**: Monitor application and dependency health
- **Metrics**: Application performance metrics via Actuator
- **Logging**: Structured logging with correlation IDs
- **Circuit Breaker Events**: Monitor resilience patterns

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/example/bridgeservice/
│   │   ├── client/           # HTTP clients for external apps
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   ├── exception/        # Exception handling
│   │   ├── model/           # Request/Response models
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.yml  # Configuration
└── test/                   # Test classes
```

### Adding New Features

1. Create new endpoints in `BridgeController`
2. Implement orchestration logic in `BridgeOrchestrationService`
3. Add client methods in `ApplicationAClient` or `ApplicationBClient`
4. Update configuration in `application.yml`

## Testing

Run tests with:

```bash
mvn test
```

For integration testing, ensure target applications are running or use mock services.

## Production Deployment

1. **Configure environment variables** for external service URLs and API keys
2. **Set up monitoring** using Actuator endpoints
3. **Configure logging** for your environment
4. **Set appropriate resource limits**
5. **Use production profile** with optimized settings

## Contributing

1. Follow the existing code structure and patterns
2. Add tests for new functionality
3. Update documentation as needed
4. Use proper error handling and logging

## License

This project is licensed under the MIT License.
