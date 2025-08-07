# Cash Service

## Description
The Cash Service handles deposit and withdrawal operations for bank accounts. It provides secure cash transaction processing with fraud detection integration and maintains transaction synchronization with the accounts service.

### Key Features:
- Cash deposit operations
- Cash withdrawal operations with balance validation
- Integration with fraud detection (blocker-service)
- Transaction notifications
- Account balance synchronization
- Session management integration
- Comprehensive transaction logging

## Port
Default port: `8092`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL database running
- Config Server running on localhost:8888
- Eureka Server running on localhost:8761
- Accounts Service running
- Blocker Service running (for fraud detection)

### Environment Variables
For local development, set the following environment variable:
- `CONFIG_SERVER_PATH=localhost`

### Startup Commands

#### Option 1: Using Maven
```bash
cd cash-service
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd cash-service
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/cash-service-0.0.1-SNAPSHOT.jar
```

### API Endpoints
- `POST /api/cash/deposit` - Deposit money to account
- `POST /api/cash/withdraw` - Withdraw money from account
- Internal sync endpoints for account balance updates

### Notes
- Integrates with blocker-service for fraud detection (75% success rate)
- Validates account balance before withdrawal operations
- Automatically sends notifications for all transactions
- Synchronizes account balances with accounts-service
- All operations require valid session authentication 