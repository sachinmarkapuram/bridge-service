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

## 🐳 Docker Support

This service is **fully containerized** with:

- **Multi-stage Dockerfile** for optimized image size
- **Docker Compose** configurations for easy deployment
- **Health checks** and monitoring built-in
- **Production-ready** with security best practices
- **Mock services** included for testing

**Quick Docker Start:**
```bash
docker build -t bridge-service:latest .
docker compose -f docker-compose.simple.yml up -d
```

📖 **See [DOCKER.md](DOCKER.md) for complete Docker documentation**

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

**For Local Development:**
- Java 17 or later
- Maven 3.6 or later

**For Docker Deployment:**
- Docker Desktop or Docker Engine
- Docker Compose (included with Docker Desktop)

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

The service is fully containerized with Docker support. See [DOCKER.md](DOCKER.md) for comprehensive deployment instructions.

#### Quick Start with Docker

```bash
# Build the Docker image
docker build -t bridge-service:latest .

# Run with simple compose (development mode)
docker compose -f docker-compose.simple.yml up -d

# Check container status
docker ps

# View application logs
docker logs bridge-service

# Test health endpoint
curl http://localhost:8080/actuator/health

# Stop the service
docker compose -f docker-compose.simple.yml down
```

#### Production Deployment

```bash
# Full stack with mock services
docker compose up -d

# Or run with custom environment variables
docker run -d \
  --name bridge-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e APPLICATION_A_URL=http://app-a-service:8080 \
  -e APPLICATION_B_URL=http://app-b-service:8080 \
  -e APPLICATION_A_API_KEY=your-secret-key-a \
  -e APPLICATION_B_API_KEY=your-secret-key-b \
  -v $(pwd)/logs:/app/logs \
  --restart unless-stopped \
  bridge-service:latest
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
