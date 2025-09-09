# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven build files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (for better caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create logs directory
RUN mkdir -p /app/logs

# Expose the application port
EXPOSE 8080

# Create non-root user for security
RUN addgroup --system bridge && adduser --system --group bridge
RUN chown -R bridge:bridge /app
USER bridge

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/bridge/ping || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/target/bridge-service-1.0.0.jar"]

# Optional: add JVM options for production
# ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "/app/target/bridge-service-1.0.0.jar"]
