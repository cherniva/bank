# Transfer Service

## Description
The Transfer Service handles money transfers between user accounts with automatic currency conversion. It integrates with exchange rates, performs fraud detection, and ensures secure peer-to-peer transfers within the bank system.

### Key Features:
- Money transfers between user accounts
- Multi-currency support with automatic conversion
- Real-time exchange rate integration
- Fraud detection integration
- Balance validation and insufficient funds protection
- Transaction notifications
- Account synchronization across services

## Port
Default port: `8094`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL database running
- Config Server running on localhost:8888
- Eureka Server running on localhost:8761
- Accounts Service running
- Exchange Service running (for currency rates)
- Blocker Service running (for fraud detection)

### Environment Variables
For local development, set the following environment variable:
- `CONFIG_SERVER_PATH=localhost`

### Startup Commands

#### Option 1: Using Maven
```bash
cd transfer-service
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd transfer-service
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/transfer-service-0.0.1-SNAPSHOT.jar
```

### API Endpoints
- `POST /api/transfer` - Transfer money between accounts
- Internal sync endpoints for account balance updates

### Transfer Flow
1. Validates source account balance
2. Checks fraud detection via blocker-service
3. Retrieves current exchange rates
4. Calculates converted amounts
5. Updates both source and destination accounts
6. Synchronizes with accounts-service and cash-service
7. Sends notifications to both users

### Notes
- Supports transfers between different currencies (RUB, USD, CNY)
- Exchange rates are fetched from exchange-service
- All transfers are subject to fraud detection
- Automatically handles currency conversion calculations
- Maintains transaction atomicity across multiple services 