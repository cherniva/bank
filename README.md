# Bank Microservices System

## Description
A comprehensive microservices-based banking system built with Spring Boot and Spring Cloud. The system provides a complete banking experience including user management, account operations, money transfers with currency conversion, fraud detection, and real-time notifications.

## Architecture Overview

### 🏗️ Microservices Architecture
The system follows a distributed microservices pattern with the following components:

#### Infrastructure Services
- **Config Server** (Port 8888) - Centralized configuration management
- **Eureka Server** (Port 8761) - Service registry and discovery
- **API Gateway** (Port 9001) - Single entry point and routing

#### Business Services
- **Accounts Service** (Port 8091) - User authentication and account management
- **Cash Service** (Port 8092) - Deposit and withdrawal operations
- **Transfer Service** (Port 8094) - Money transfers with currency conversion
- **Exchange Service** (Port 8093) - Real-time exchange rates
- **Exchange Generator Service** (Port 8097) - Automatic rate generation
- **Blocker Service** (Port 8095) - Fraud detection
- **Notifications Service** (Port 8096) - Centralized notifications

#### User Interface
- **Front UI** (Port 8090) - Web-based banking interface

#### Shared Components
- **Common Module** - Shared entities, DTOs, repositories, and mappers

### 🛠️ Technology Stack
- **Java 21** - Programming language
- **Spring Boot 3.5.3** - Application framework
- **Spring Cloud 2025.0.0** - Microservices framework
- **PostgreSQL** - Database
- **Keycloak** - Identity and access management
- **Docker & Docker Compose** - Containerization
- **Maven** - Build tool
- **Thymeleaf** - Frontend templating

### 💰 Key Features
- **Multi-currency Support** - USD, RUB, CNY
- **Real-time Exchange Rates** - Automatic rate updates
- **Fraud Detection** - Transaction blocking simulation
- **Session Management** - Secure user sessions
- **Notifications** - Real-time transaction notifications
- **Responsive UI** - Modern web interface
- **Microservices Pattern** - Scalable distributed architecture

## 🚀 Quick Start with Docker

### Prerequisites
- Docker
- Docker Compose

### Starting the Entire System

#### Option 1: Build and Start (Recommended)
```bash
docker-compose up --build
```

#### Option 2: Start with Pre-built Images
```bash
docker-compose up
```

#### Option 3: Start in Background
```bash
docker-compose up -d --build
```

### 🔗 Access URLs
Once all services are running:

- **Banking Web App**: http://localhost:8090
- **API Gateway**: http://localhost:9001
- **Eureka Dashboard**: http://localhost:8761
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **PostgreSQL**: localhost:5433 (postgres/1)

### 🗃️ Database Initialization
The PostgreSQL database is automatically initialized with the required schema using the `init.sql` script.

### 🛑 Stopping the System
```bash
docker-compose down
```

### 🧹 Clean Up (Remove Volumes)
```bash
docker-compose down -v
```

## 📚 Service Documentation

Each service has its own README with detailed information:

- [Config Server](config-server/README.md)
- [Eureka Server](eureka-server/README.md)
- [API Gateway](api-gateway/README.md)
- [Accounts Service](accounts-service/README.md)
- [Cash Service](cash-service/README.md)
- [Transfer Service](transfer-service/README.md)
- [Exchange Service](exchange-service/README.md)
- [Exchange Generator Service](exchange-generator-service/README.md)
- [Blocker Service](blocker-service/README.md)
- [Notifications Service](notifications-service/README.md)
- [Front UI](front-ui/README.md)
- [Common Module](common/README.md)

## 🏃‍♂️ Local Development

For local development without Docker, see individual service READMEs for startup instructions. Services must be started in this order:

1. **PostgreSQL Database**
2. **Config Server**
3. **Eureka Server**
4. **API Gateway**
5. **All Business Services** (can be started in parallel)
6. **Front UI**

## 🎯 Usage Examples

### Creating a User Account
1. Navigate to http://localhost:8090
2. Click "Register" to create a new account
3. Log in with your credentials
4. Create bank accounts in different currencies

### Making Transactions
1. **Deposits**: Add money to your accounts
2. **Withdrawals**: Remove money (with balance validation)
3. **Transfers**: Send money to other users with automatic currency conversion

### Monitoring
- **Eureka Dashboard**: View all registered services
- **Notifications**: See real-time transaction notifications
- **Account Balances**: Track your account balances across currencies

## 🐛 Troubleshooting

### Common Issues

1. **Services not starting**: Check if all required ports are available
2. **Database connection issues**: Ensure PostgreSQL container is running
3. **Service discovery issues**: Verify Eureka Server is accessible
4. **Configuration issues**: Check Config Server logs

### Viewing Logs
```bash
# View logs for specific service
docker-compose logs [service-name]

# Follow logs in real-time
docker-compose logs -f [service-name]

# View all logs
docker-compose logs
```

### Health Checks
Most services provide health check endpoints:
- Config Server: http://localhost:8888/actuator/health
- Other services: http://localhost:[port]/actuator/health

## 🏗️ Development & Build

### Building Individual Services
```bash
mvn clean package -DskipTests
```

### Running Tests
```bash
mvn test
```

### Building Common Module First
```bash
cd common
mvn clean install
```