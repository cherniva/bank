# Multi-stage build for Blocker Service
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
RUN mvn dependency:go-offline -pl blocker-service

# Copy source code for this service only
COPY blocker-service/src ./blocker-service/src

# Build the application
RUN mvn clean package -pl blocker-service -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar file
COPY --from=builder /app/blocker-service/target/*.jar app.jar

# Expose port
EXPOSE 8095

# Environment variables

# Environment variables
ENV JAVA_OPTS="-Dspring.profiles.active=docker"
ENV CONFIG_SERVER_PATH="config-server"
CMD java $JAVA_OPTS -jar app.jar