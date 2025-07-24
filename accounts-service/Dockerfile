# Multi-stage build for Accounts Service
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

# Download dependencies for this specific service
RUN mvn dependency:go-offline -pl accounts-service

# Copy source code for this service only
COPY accounts-service/src ./accounts-service/src

# Build the application
RUN mvn clean package -pl accounts-service -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the jar file
COPY --from=builder /app/accounts-service/target/*.jar app.jar

# Expose port
EXPOSE 8091

# Environment variables
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="postgres"
ENV CONFIG_SERVER_URL="http://config-server:8888"
ENV EUREKA_SERVER_URL="http://eureka-server:8761/eureka"
ENV DATABASE_URL="jdbc:postgresql://postgres:5432/bank"
ENV DATABASE_USERNAME="postgres"
ENV DATABASE_PASSWORD="1"

# Run the application (as root - simpler approach)
CMD java $JAVA_OPTS -jar app.jar 