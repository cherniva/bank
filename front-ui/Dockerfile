# Multi-stage build for Front UI
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

# Install the parent POM first (needed by common module)
RUN mvn clean install -N -DskipTests

# Build and install the common module first so it's available as a dependency
RUN mvn clean install -pl common -DskipTests

# Download dependencies for this specific service
RUN mvn dependency:go-offline -pl front-ui

# Copy source code for this service only
COPY front-ui/src ./front-ui/src

# Build the application
RUN mvn clean package -pl front-ui -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar file
COPY --from=builder /app/front-ui/target/*.jar app.jar

# Expose port
EXPOSE 8090

# Environment variables
ENV JAVA_OPTS="-Dspring.profiles.active=docker"
ENV CONFIG_SERVER_PATH="config-server"
CMD java $JAVA_OPTS -jar app.jar
