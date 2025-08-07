# API Gateway

## Description
The API Gateway is the single entry point for all client requests in the Bank microservices architecture. It handles request routing, authentication, authorization, rate limiting, and load balancing across all backend services.

### Key Features:
- Single entry point for all API requests
- OAuth2 authentication and authorization integration
- Service routing and load balancing
- Request/response transformation
- Circuit breaker pattern implementation
- Cross-origin resource sharing (CORS) handling
- Centralized security policies

## Port
Default port: `9001`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Config Server running on localhost:8888
- Eureka Server running on localhost:8761

### Environment Variables
For local development, set the following environment variable:
- `CONFIG_SERVER_PATH=localhost`

### Startup Commands

#### Option 1: Using Maven
```bash
cd api-gateway
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd api-gateway
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/api-gateway-0.0.1-SNAPSHOT.jar
```

### Notes
- Must be started after config-server and eureka-server
- Acts as the main entry point for all client applications
- Routes requests to appropriate microservices
- Test endpoint available at: `http://localhost:9001/test`
- All microservice APIs are accessible through this gateway 