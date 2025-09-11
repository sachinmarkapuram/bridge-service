# Bridge Service - Docker Deployment Guide

This guide provides instructions for containerizing and deploying the Bridge Service using Docker.

## Prerequisites

### Install Docker Desktop

1. **macOS**: Download Docker Desktop from [https://docs.docker.com/desktop/mac/install/](https://docs.docker.com/desktop/mac/install/)
2. **Windows**: Download Docker Desktop from [https://docs.docker.com/desktop/windows/install/](https://docs.docker.com/desktop/windows/install/)
3. **Linux**: Follow instructions at [https://docs.docker.com/engine/install/](https://docs.docker.com/engine/install/)

After installation, verify Docker is working:
```bash
docker --version
docker-compose --version
```

## Building the Docker Image

### 1. Build the Image
```bash
docker build -t bridge-service:latest .
```

### 2. Verify the Image
```bash
docker images | grep bridge-service
```

## Running the Container

### Option 1: Simple Docker Run
```bash
# Run with default development profile
docker run -d \
  --name bridge-service \
  -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  bridge-service:latest

# Check container status
docker ps
docker logs bridge-service
```

### Option 2: Using Docker Compose (Simple)
```bash
# Run just the bridge service
docker-compose -f docker-compose.simple.yml up -d

# View logs
docker-compose -f docker-compose.simple.yml logs -f bridge-service

# Stop the service
docker-compose -f docker-compose.simple.yml down
```

### Option 3: Using Docker Compose (Full Stack)
```bash
# Run bridge service with mock applications
docker-compose up -d

# View logs for all services
docker-compose logs -f

# Stop all services
docker-compose down
```

## Configuration

### Environment Variables
The containerized application supports the following environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `development` | Spring profile (development/production) |
| `APPLICATION_A_URL` | `http://localhost:8081` | URL for Application A |
| `APPLICATION_B_URL` | `http://localhost:8082` | URL for Application B |
| `APPLICATION_A_API_KEY` | - | API key for Application A |
| `APPLICATION_B_API_KEY` | - | API key for Application B |
| `SERVICE_TIMEOUT` | `10000` | Service timeout in milliseconds |
| `MAX_RETRIES` | `3` | Maximum retry attempts |
| `JAVA_OPTS` | `-Xmx512m -Xms256m` | JVM options |

### Production Deployment
```bash
docker run -d \
  --name bridge-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e APPLICATION_A_URL=http://your-app-a-service:8080 \
  -e APPLICATION_B_URL=http://your-app-b-service:8080 \
  -e APPLICATION_A_API_KEY=your-secret-key-a \
  -e APPLICATION_B_API_KEY=your-secret-key-b \
  -e SERVICE_TIMEOUT=15000 \
  -e MAX_RETRIES=5 \
  -v $(pwd)/logs:/app/logs \
  --restart unless-stopped \
  bridge-service:latest
```

## Health Checks

The container includes built-in health checks:

### Docker Health Check
The Dockerfile includes a health check that monitors the `/actuator/health` endpoint.

### Manual Health Check
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check detailed health information
curl http://localhost:8080/actuator/health | jq .
```

## Logging

### Container Logs
```bash
# View container logs
docker logs bridge-service

# Follow logs in real-time
docker logs -f bridge-service

# View logs with timestamps
docker logs -t bridge-service
```

### Application Logs
Application logs are also written to files and can be accessed via mounted volumes:
```bash
# View application log file
tail -f logs/bridge-service.log
```

## Troubleshooting

### Common Issues

1. **Port already in use**
   ```bash
   # Find process using port 8080
   lsof -i :8080
   # Kill the process or use a different port
   docker run -p 8081:8080 bridge-service:latest
   ```

2. **Container fails to start**
   ```bash
   # Check container logs
   docker logs bridge-service
   
   # Inspect container
   docker inspect bridge-service
   ```

3. **Health check failures**
   ```bash
   # Check if application is running
   docker exec -it bridge-service curl http://localhost:8080/actuator/health
   
   # Check application logs
   docker logs bridge-service | grep -i error
   ```

### Container Management

```bash
# Stop container
docker stop bridge-service

# Remove container
docker rm bridge-service

# Remove image
docker rmi bridge-service:latest

# Clean up unused containers and images
docker system prune
```

## Multi-Stage Build Benefits

The Dockerfile uses a multi-stage build which provides:

1. **Smaller final image**: Only includes JRE and the built JAR
2. **Better security**: No build tools in the final image
3. **Faster builds**: Better layer caching
4. **Optimized for containers**: Includes container-aware JVM flags

## Kubernetes Deployment

For Kubernetes deployment, you can use the generated Docker image:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bridge-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: bridge-service
  template:
    metadata:
      labels:
        app: bridge-service
    spec:
      containers:
      - name: bridge-service
        image: bridge-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: APPLICATION_A_URL
          value: "http://app-a-service:8080"
        - name: APPLICATION_B_URL
          value: "http://app-b-service:8080"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

## Next Steps

1. Install Docker Desktop on your system
2. Build the Docker image: `docker build -t bridge-service:latest .`
3. Test with: `docker-compose -f docker-compose.simple.yml up -d`
4. Verify health: `curl http://localhost:8080/actuator/health`
5. Deploy to your target environment
