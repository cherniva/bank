# Multi-stage build for API Gateway
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy all pom.xml files first for better layer caching and module validation
COPY pom.xml .
COPY common/pom.xml ./common/
COPY config-server/pom.xml ./config-server/
COPY eureka-server/pom.xml ./eureka-server/
COPY api-gateway/pom.xml ./api-gateway/
COPY accounts-service/pom.xml ./accounts-service/
COPY cash-service/pom.xml ./cash-service/
COPY transfer-service/pom.xml ./transfer-service/
COPY exchange-service/pom.xml ./exchange-service/
COPY exchange-generator-service/pom.xml ./exchange-generator-service/
COPY blocker-service/pom.xml ./blocker-service/
COPY notifications-service/pom.xml ./notifications-service/
COPY front-ui/pom.xml ./front-ui/

# Copy common module source (needed by other modules)
COPY common/src ./common/src

# Install the parent POM first (needed by all modules)
RUN mvn clean install -N -DskipTests

# Download dependencies for this specific service
RUN mvn dependency:go-offline -pl api-gateway

# Copy source code for this service only
COPY api-gateway/src ./api-gateway/src

# Build the application
RUN mvn clean package -pl api-gateway -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar file
COPY --from=builder /app/api-gateway/target/*.jar app.jar

# Expose port
EXPOSE 9001

# Environment variables
ENV JAVA_OPTS=""

# Run the application (as root - simpler approach)
CMD java $JAVA_OPTS -jar app.jar --spring.profiles.active=docker --spring.cloud.config.uri=http://config-server:8888