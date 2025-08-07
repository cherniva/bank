# Eureka Server

## Description
The Eureka Server is a service registry and discovery server for the Bank microservices architecture. It enables service registration and discovery, allowing microservices to find and communicate with each other without hardcoded hostnames and ports.

### Key Features:
- Service registration and discovery
- Load balancing support
- Health monitoring and failover
- Service location transparency
- Dynamic service scaling support

## Port
Default port: `8761`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Environment Variables
For local development, set the following environment variable:
- `HOST=localhost`

### Startup Commands

#### Option 1: Using Maven
```bash
cd eureka-server
HOST=localhost mvn spring-boot:run
```

#### Option 2: Using JAR
```bash
cd eureka-server
mvn clean package -DskipTests
HOST=localhost java -jar target/eureka-server-0.0.1-SNAPSHOT.jar
```

### Notes
- Should be started after config-server but before other business services
- Eureka dashboard available at: `http://localhost:8761`
- All other microservices will register themselves with this Eureka server
- The `HOST=localhost` environment variable is required for local development 