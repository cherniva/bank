# Multi-stage build for Notifications Service
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
RUN mvn dependency:go-offline -pl notifications-service

# Copy source code for this service only
COPY notifications-service/src ./notifications-service/src

# Build the application
RUN mvn clean package -pl notifications-service -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar file
COPY --from=builder /app/notifications-service/target/*.jar app.jar

# Expose port
EXPOSE 8096

# Environment variables
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"
ENV SPRING_APPLICATION_NAME="notifications-service"
ENV CONFIG_SERVER_URL="http://config-server:8888"
ENV EUREKA_SERVER_URL="http://eureka-server:8761/eureka"

# Run the application (as root - simpler approach)
CMD java $JAVA_OPTS -jar app.jar 