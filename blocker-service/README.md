# Blocker Service

## Description
The Blocker Service provides fraud detection and transaction blocking functionality for the bank system. It simulates a fraud detection system that randomly blocks suspicious transactions to demonstrate security measures in financial operations.

### Key Features:
- Fraud detection simulation
- Random transaction blocking (25% block rate)
- Risk assessment for financial operations
- Simple boolean response for transaction approval
- Integration with cash and transfer services

## Port
Default port: `8095`

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
cd blocker-service
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd blocker-service
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/blocker-service-0.0.1-SNAPSHOT.jar
```

### API Endpoints
- `GET /blocker/check` - Check if transaction should be blocked

### Fraud Detection Logic
- **Approval Rate**: 75% (3 out of 4 transactions approved)
- **Block Rate**: 25% (1 out of 4 transactions blocked)
- **Response**: Boolean value (true = approved, false = blocked)

### Notes
- Used by cash-service and transfer-service for fraud detection
- Provides simple random blocking to simulate real fraud detection
- In production, this would contain sophisticated fraud detection algorithms
- Essential for demonstrating security measures in financial transactions
- No authentication required for internal service calls 