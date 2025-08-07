# Notifications Service

## Description
The Notifications Service manages and stores notifications for users across all banking operations. It receives notification requests from various services and provides APIs for retrieving and managing user notifications.

### Key Features:
- Centralized notification management
- Multi-service notification support (accounts, cash, transfer)
- In-memory notification storage
- User-specific notification retrieval
- Read/unread status tracking
- Automatic message generation for different operation types
- Multilingual support (Russian messages)

## Port
Default port: `8096`

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
cd notifications-service
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd notifications-service
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/notifications-service-0.0.1-SNAPSHOT.jar
```

### API Endpoints
- `POST /notifications/accounts` - Receive notifications from accounts service
- `POST /notifications/transfer` - Receive notifications from transfer service  
- `POST /notifications/cash` - Receive notifications from cash service
- `GET /notifications/user/{userId}` - Get notifications for a user
- `PUT /notifications/{notificationId}/read` - Mark notification as read

### Supported Notification Types
- **Account Operations**: Account creation, user registration
- **Cash Operations**: Deposits, withdrawals
- **Transfer Operations**: Money transfers between accounts

### Notes
- Receives notifications from accounts-service, cash-service, and transfer-service
- Stores notifications in memory (would use database in production)
- Generates localized messages in Russian
- Used by front-ui to display user notifications
- Supports marking notifications as read/unread 