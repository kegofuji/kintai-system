# Multi-stage Dockerfile for 勤怠管理システム

# ========== Java Spring Boot Build Stage ==========
FROM openjdk:17-jdk-alpine AS java-builder
WORKDIR /app/java

# Maven wrapper and pom.xml copy
COPY backend/java-springboot/mvnw .
COPY backend/java-springboot/.mvn .mvn
COPY backend/java-springboot/pom.xml .

# Download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY backend/java-springboot/src src
RUN ./mvnw clean package -DskipTests

# ========== Python FastAPI Build Stage ==========
FROM python:3.11-slim AS python-builder
WORKDIR /app/python

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    libffi-dev \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY backend/python-fastapi/requirements.txt .
RUN pip install --no-cache-dir --user -r requirements.txt

# Copy Python application
COPY backend/python-fastapi .

# ========== Frontend Build Stage (Optional) ==========
FROM node:18-alpine AS frontend-builder
WORKDIR /app/frontend

# Note: Frontend is Vanilla JS, no build step needed
# Just copy static files
COPY frontend/public_ui .

# ========== Final Runtime Stage ==========
FROM openjdk:17-jre-alpine AS runtime

# Install Python and system dependencies
RUN apk add --no-cache \
    python3 \
    py3-pip \
    curl \
    bash \
    tzdata \
    && ln -sf python3 /usr/bin/python

# Set timezone to Asia/Tokyo 
ENV TZ=Asia/Tokyo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create application directories
RUN mkdir -p /app/java /app/python /app/frontend
WORKDIR /app

# Copy built artifacts
COPY --from=java-builder /app/java/target/*.jar /app/java/app.jar
COPY --from=python-builder /root/.local /root/.local
COPY --from=python-builder /app/python /app/python
COPY --from=frontend-builder /app/frontend /app/frontend

# Copy static frontend to Spring Boot static resources
RUN mkdir -p /app/java/static && cp -r /app/frontend/* /app/java/static/

# Set Python path
ENV PATH=/root/.local/bin:$PATH

# Create startup script
RUN cat > /app/start.sh << 'EOF'
#!/bin/bash
set -e

# Start Python FastAPI server in background
echo "Starting FastAPI server on port 8081..."
cd /app/python
python -m uvicorn main:app --host 0.0.0.0 --port 8081 &
FASTAPI_PID=$!

# Wait for FastAPI to start
sleep 5

# Start Java Spring Boot server
echo "Starting Spring Boot server on port 8080..."
cd /app/java
java -Djava.security.egd=file:/dev/./urandom \
     -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production} \
     -Dserver.port=${PORT:-8080} \
     -jar app.jar &
SPRING_PID=$!

# Function to handle shutdown
shutdown() {
    echo "Shutting down services..."
    kill -TERM $FASTAPI_PID 2>/dev/null || true
    kill -TERM $SPRING_PID 2>/dev/null || true
    wait $FASTAPI_PID 2>/dev/null || true
    wait $SPRING_PID 2>/dev/null || true
    exit 0
}

# Set up signal handlers
trap shutdown SIGTERM SIGINT

# Wait for both processes
wait $SPRING_PID
EOF

RUN chmod +x /app/start.sh

# Environment variables 
ENV SPRING_PROFILES_ACTIVE=production
ENV PORT=8080
ENV FASTAPI_PORT=8081
ENV DATABASE_URL=""
ENV DATABASE_USERNAME=""
ENV DATABASE_PASSWORD=""

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose ports
EXPOSE 8080 8081

# Start command
CMD ["/app/start.sh"]