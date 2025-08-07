# Front UI

## Description
The Front UI is a web-based user interface for the Bank system. It provides a complete banking experience including user authentication, account management, cash operations, transfers, and notifications through a responsive web interface built with Spring Boot and Thymeleaf.

### Key Features:
- User authentication and registration
- Account dashboard and management
- Cash operations (deposit/withdrawal)
- Money transfers between accounts
- Real-time notifications
- User profile management
- Session management with cookies
- Responsive web design

## Port
Default port: `8090`

## How to Start Locally

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Config Server running on localhost:8888
- Eureka Server running on localhost:8761
- API Gateway running on localhost:9001
- All backend services running (accounts, cash, transfer, etc.)

### Environment Variables
For local development, set the following environment variable:
- `CONFIG_SERVER_PATH=localhost`

### Startup Commands

#### Option 1: Using Maven
```bash
cd front-ui
CONFIG_SERVER_PATH=localhost mvn spring-boot:run -Dspring.profiles.active=local
```

#### Option 2: Using JAR
```bash
cd front-ui
mvn clean package -DskipTests
CONFIG_SERVER_PATH=localhost java -Dspring.profiles.active=local -jar target/front-ui-0.0.1-SNAPSHOT.jar
```

### Web Interface
Access the application at: `http://localhost:8090`

### Main Features
- **Login/Registration**: User authentication system
- **Dashboard**: Account overview and balance information
- **Cash Operations**: Deposit and withdraw money
- **Transfers**: Send money to other users with currency conversion
- **Account Management**: Create accounts in different currencies
- **Notifications**: View transaction notifications
- **Profile Management**: Edit user information and password

### Navigation
- `/` or `/main` - Main dashboard (redirects to login if not authenticated)
- `/login` - User login page
- `/register` - User registration page

### Notes
- Requires all backend services to be running for full functionality
- Uses session-based authentication with HTTP cookies
- Communicates with backend services through API Gateway
- Built with Thymeleaf templates for server-side rendering
- No database connection required (uses backend services for data) 