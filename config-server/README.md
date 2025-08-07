# Config Server

## Description
The Config Server is a centralized configuration management service for the Bank microservices architecture. It provides externalized configuration management for all distributed services in the system using Spring Cloud Config Server.

### Key Features:
- Centralized configuration management for all microservices
- Version-controlled configuration using Git repository
- Environment-specific configuration profiles
- Dynamic configuration updates without service restarts
- Secure configuration management

## Port
Default port: `8888`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Startup Commands

#### Option 1: Using Maven
```bash
cd config-server
mvn spring-boot:run
```

#### Option 2: Using JAR
```bash
cd config-server
mvn clean package -DskipTests
java -jar target/config-server-0.0.1-SNAPSHOT.jar
```

### Notes
- Config Server should be started **FIRST** before other services as they depend on it for configuration
- No special environment variables required for local startup
- The service runs on port 8888 by default
- Health check available at: `http://localhost:8888/actuator/health` 