# Guidance.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Development Commands

### Building & Running

```bash
# Clean and compile the project
mvn clean compile

# Run the application (development profile by default)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=production

# Package the application
mvn clean package

# Build and skip tests (for quick builds)
mvn clean package -DskipTests
```

### Testing

```bash
# Run all unit tests
mvn test

# Run integration tests specifically
mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=integration -Djacoco.skip=true

# Run specific test classes
mvn test -Dtest=BridgeServiceIntegrationTest -Dspring.profiles.active=integration -Djacoco.skip=true
mvn test -Dtest=LondonStockExchangeServiceIntegrationTest -Dspring.profiles.active=integration -Djacoco.skip=true

# Run all integration tests
mvn test -Dtest="BridgeServiceIntegrationTest,LondonStockExchangeServiceIntegrationTest" -Dspring.profiles.active=integration -Djacoco.skip=true

# Run tests with coverage
mvn clean test jacoco:report

# Run full Maven lifecycle with integration tests
mvn clean verify

# Quick test specific method or class
mvn test -Dtest=ClassName#methodName

# Run tests with detailed output
mvn test -Dspring.profiles.active=test -X
```

### Docker Operations

```bash
# Build Docker image
docker build -t bridge-service:latest .

# Run with simple compose (development mode)
docker compose -f docker-compose.simple.yml up -d

# Run full stack with mock services
docker compose up -d

# View logs
docker logs bridge-service
docker compose logs -f

# Stop services
docker compose -f docker-compose.simple.yml down
docker compose down

# Health check
curl http://localhost:8080/actuator/health
```

### Dependency Management

```bash
# Check for dependency updates
mvn versions:display-dependency-updates

# Run OWASP security scan
mvn org.owasp:dependency-check-maven:check

# View dependency tree
mvn dependency:tree
```

### Development Workflow

```bash
# Quick development cycle (compile + run without packaging)
mvn compile exec:java -Dexec.mainClass="com.example.bridgeservice.BridgeServiceApplication"

# Hot reload during development (requires spring-boot-devtools)
mvn spring-boot:run -Dspring-boot.run.fork=false

# Check application logs
tail -f logs/bridge-service.log

# Quick health check
curl -s http://localhost:8080/actuator/health | jq .

# View all actuator endpoints
curl -s http://localhost:8080/actuator | jq .

# Check circuit breaker status
curl -s http://localhost:8080/actuator/circuitbreakers | jq .

# Test API endpoints quickly
curl -X POST http://localhost:8080/api/bridge/route \
  -H "Content-Type: application/json" \
  -d '{"operation": "test", "targetApplication": "a", "data": {"test": "data"}}' | jq .
```

## Architecture Overview

### Service Pattern
This is a **Spring Boot microservice** that acts as a **bridge/orchestration service** between multiple stateless applications and external APIs. It follows a clean layered architecture pattern:

- **Controller Layer** (`BridgeController`) - REST API endpoints
- **Service Layer** (`BridgeOrchestrationService`, `LondonStockExchangeService`) - Business logic and orchestration
- **Client Layer** (`ApplicationAClient`, `ApplicationBClient`) - External service integration
- **Configuration Layer** - Spring configuration and async thread pool management

### Core Capabilities

1. **Request Routing** - Route requests to specific applications (A or B)
2. **Sequential Coordination** - Orchestrate data flow (A→B or B→A)
3. **Parallel Execution** - Execute operations on both applications simultaneously
4. **Asynchronous Integration** - Non-blocking external API integration (LSE)
5. **Resilience Patterns** - Circuit breakers, retries, fallback handling

### Key Components

**BridgeOrchestrationService**: Main orchestration engine that coordinates between different applications and handles business logic flow.

**Asynchronous Processing**: 
- Dedicated thread pools for LSE operations (`lseAsyncExecutor`) and general async tasks (`taskExecutor`)
- CompletableFuture-based async operations with correlation ID tracking
- Non-blocking HTTP 202 responses for long-running operations

**Resilience Patterns**:
- Circuit breakers for each external application with configurable failure thresholds
- Retry logic with exponential backoff
- Graceful degradation and fallback responses

**External Integrations**:
- Applications A & B via HTTP clients with resilience patterns
- London Stock Exchange API with async processing
- WebClient (reactive) and RestTemplate (blocking) HTTP clients

### Configuration Architecture

**Profile-based Configuration**:
- `development` - Local development with localhost URLs
- `production` - Production with environment variable injection
- `integration` - Test environment with optimized settings

**Environment Variable Mapping**:
```yaml
APPLICATION_A_URL -> bridge.services.application-a.base-url
APPLICATION_A_API_KEY -> bridge.services.application-a.api-key
APPLICATION_B_URL -> bridge.services.application-b.base-url
APPLICATION_B_API_KEY -> bridge.services.application-b.api-key
SERVICE_TIMEOUT -> bridge.services.timeout-millis
MAX_RETRIES -> bridge.services.max-retries
```

## API Endpoints

### Core Operations
- `POST /api/bridge/route` - Route request to specific application
- `POST /api/bridge/coordinate/a-to-b` - Sequential A→B coordination
- `POST /api/bridge/coordinate/b-to-a` - Sequential B→A coordination
- `POST /api/bridge/coordinate/parallel` - Parallel execution

### Asynchronous Operations
- `GET /api/bridge/initiate` - Initiate LSE data fetch (returns HTTP 202)
  - Query params: `dataType={market|stock}`, `symbol={SYMBOL}` (required for stock)

### Monitoring
- `GET /api/bridge/health` - Application health check
- `GET /api/bridge/info` - Service information and endpoints
- `GET /api/bridge/ping` - Simple ping/pong
- `GET /actuator/health` - Spring Boot health endpoint
- `GET /actuator/circuitbreakers` - Circuit breaker status
- `GET /actuator/retries` - Retry statistics

## Common Development Tasks

### Adding New Endpoints
1. Add method in `BridgeController` with appropriate mapping
2. Implement logic in `BridgeOrchestrationService`
3. Add client methods if external services are involved
4. Update configuration if needed
5. Add comprehensive tests (both unit and integration)

### Adding External Service Integration
1. Create client class (follow pattern of `ApplicationAClient`/`ApplicationBClient`)
2. Configure circuit breaker and retry in `application.yml`
3. Add service class for business logic (follow `LondonStockExchangeService` pattern)
4. For async operations, use dedicated thread pools in `AsyncConfig`
5. Add integration tests

### Testing Strategy
- **Unit Tests**: Component-level testing with mocking
- **Integration Tests**: Full application context with test profile
- **Test Profiles**: Use `integration` profile for integration tests
- **Mock Services**: Docker-based mock services for external dependencies
- **Coverage**: JaCoCo plugin configured for test coverage reports

### Async Operation Patterns
- Use correlation IDs for tracking across async boundaries
- Return HTTP 202 for async initiations with correlation ID
- Use CompletableFuture for non-blocking operations
- Handle exceptions with proper logging and fallback responses
- Configure dedicated thread pools for different operation types

## CI/CD Integration

This project includes comprehensive CI/CD with GitHub Actions:
- **Unit & Integration Tests** on all PRs and pushes
- **OWASP Security Scanning** for dependency vulnerabilities
- **Multi-stage Docker Builds** with optimization
- **Google Cloud Run Deployment** for staging (`docker_setup`/`develop`) and production (`main`)
- **Slack Notifications** for deployment status

See `CICD.md` for complete pipeline documentation.

## Docker Architecture

**Multi-stage Dockerfile** optimized for production:
- Build stage with Maven and OpenJDK 17
- Runtime stage with OpenJDK 17 JRE
- Non-root user for security
- Health checks and proper signal handling
- Optimized JVM flags for containerized environments

See `DOCKER.md` for complete containerization guide.

## Local Development Environment

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker Desktop (for containerized testing)

### Quick Start
```bash
# Start the application
mvn spring-boot:run

# In another terminal, test health endpoint
curl http://localhost:8080/actuator/health

# Test a simple route request
curl -X POST http://localhost:8080/api/bridge/route \
  -H "Content-Type: application/json" \
  -d '{"operation": "test", "targetApplication": "a", "data": {"test": "data"}}'
```

### Environment Variables for Development
```bash
export APPLICATION_A_URL=http://localhost:8081
export APPLICATION_B_URL=http://localhost:8082
export APPLICATION_A_API_KEY=dev-key-a
export APPLICATION_B_API_KEY=dev-key-b
```

## Important Notes

### Correlation ID Tracking
All operations generate or accept correlation IDs for end-to-end tracing. Always include correlation ID in logs and responses for debugging.

### Error Handling Patterns
- Global exception handler provides consistent error responses
- Circuit breakers prevent cascade failures
- Retry logic with exponential backoff for transient failures
- Graceful degradation with fallback responses

### Async Operations
- LSE endpoints return HTTP 202 (Accepted) immediately
- Actual processing happens asynchronously in background threads
- Use correlation IDs to track async operation status
- Configure appropriate thread pool sizes based on expected load

### Configuration Management
- Use Spring profiles for environment-specific configuration
- Externalize sensitive data via environment variables
- Circuit breaker and retry configurations are per-service
- Actuator endpoints provide runtime configuration visibility
