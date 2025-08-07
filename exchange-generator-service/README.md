# Exchange Generator Service

## Description
The Exchange Generator Service automatically generates and updates exchange rates for the bank system. It simulates real-world currency fluctuations by periodically generating new rates with small variations and sends them to the exchange service.

### Key Features:
- Automatic exchange rate generation
- Realistic rate fluctuations simulation
- Scheduled rate updates every 5 seconds
- Support for multiple currency pairs
- Rate variation within realistic bounds
- Automatic delivery to exchange service

## Port
Default port: `8097`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Config Server running on localhost:8888
- Eureka Server running on localhost:8761
- Exchange Service running (to receive updates)

### Environment Variables
For local development, set the following environment variable:
- `CONFIG_SERVER_PATH=localhost`

### Startup Commands

#### Option 1: Using Maven
```bash
cd exchange-generator-service
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd exchange-generator-service
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/exchange-generator-service-0.0.1-SNAPSHOT.jar
```

### Rate Generation
- **Update Frequency**: Every 5 seconds
- **Currency Pairs**: RUB-USD, RUB-CNY
- **Rate Variation**: ±2% from base rates
- **Base Rates**: 
  - RUB-USD: Buy 0.0112, Sell 0.0108
  - RUB-CNY: Buy 0.0815, Sell 0.0792

### Notes
- Runs in the background automatically generating rate updates
- No direct API endpoints (internal service)
- Sends generated rates to exchange-service via API Gateway
- Rate variations simulate real market fluctuations
- Essential for providing dynamic exchange rates in the system 