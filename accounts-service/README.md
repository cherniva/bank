# Accounts Service

## Description
The Accounts Service is a core business service that manages user accounts, authentication, and account operations in the Bank system. It handles user registration, login, account creation, and synchronizes account data across other services.

### Key Features:
- User authentication and authorization
- User registration and profile management
- Bank account creation and management
- Session management
- Account balance tracking and updates
- Data synchronization with other services (cash, transfer)
- Account operations (deposit, withdraw, transfer)
- User account deletion and cleanup

## Port
Default port: `8091`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL database running
- Config Server running on localhost:8888
- Eureka Server running on localhost:8761

### Environment Variables
For local development, set the following environment variable:
- `CONFIG_SERVER_PATH=localhost`

### Database Setup
Ensure PostgreSQL is running with the bank database initialized using the provided `init.sql` script.

### Startup Commands

#### Option 1: Using Maven
```bash
cd accounts-service
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd accounts-service
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/accounts-service-0.0.1-SNAPSHOT.jar
```

### API Endpoints
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration
- `POST /api/accounts/addAccount` - Create new bank account
- `GET /api/users/{userId}` - Get user information
- Account sync endpoints for internal service communication

### Notes
- This is a foundational service that other services depend on
- Manages user data and account balances
- Provides authentication services for the entire system
- Automatically synchronizes account data with cash-service and transfer-service 