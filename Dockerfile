# Multi-stage build for optimized image size

# Build stage
FROM openjdk:17-jdk-slim as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and configuration
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (for better caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Install curl for health checks (minimal layer)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create application directory
WORKDIR /app

# Create non-root user for security
RUN groupadd --system --gid 1001 bridge && \
    useradd --system --uid 1001 --gid bridge --shell /bin/false bridge

# Create logs directory and set permissions
RUN mkdir -p /app/logs && chown -R bridge:bridge /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=bridge:bridge /app/target/bridge-service-1.0.0.jar /app/bridge-service.jar

# Switch to non-root user
USER bridge

# Expose the application port
EXPOSE 8080

# Health check using actuator health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/bridge-service.jar"]
