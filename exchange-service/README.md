# Exchange Service

## Description
The Exchange Service provides current exchange rates for currency conversion operations. It maintains real-time exchange rates for supported currency pairs and receives periodic updates from the exchange generator service.

### Key Features:
- Real-time exchange rate management
- Support for multiple currency pairs (RUB-USD, RUB-CNY, USD-CNY)
- Buy and sell rate tracking
- Rate update API for external services
- In-memory rate caching with fallback defaults
- Cross-currency rate calculations

## Port
Default port: `8093`

## Supported Currencies
- RUB (Russian Ruble)
- USD (US Dollar)  
- CNY (Chinese Yuan)

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
cd exchange-service
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd exchange-service
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/exchange-service-0.0.1-SNAPSHOT.jar
```

### API Endpoints
- `GET /exchange/course` - Get current exchange rates
- `POST /exchange/course/update` - Update exchange rates (internal)

### Default Exchange Rates
- RUB → USD: Buy 0.0112, Sell 0.0108
- RUB → CNY: Buy 0.0815, Sell 0.0792
- USD → CNY: Calculated automatically

### Notes
- Rates are updated automatically every 5 seconds by exchange-generator-service
- Provides both buy and sell rates for each currency pair
- Falls back to default rates if no updates are received
- Used by transfer-service for currency conversion calculations 