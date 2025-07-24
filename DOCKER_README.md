# Bank Microservices - Docker Setup (Simplified)

This guide explains how to run your Bank microservices using Docker with proper startup order and dependencies using **simplified root-user containers**.

## 📋 Prerequisites

- Docker Desktop installed and running
- Docker Compose v3.8 or higher
- At least 6GB RAM available for Docker
- Ports 8080, 8088-8097, 9001, 5432 available

## 🏗️ Architecture Overview

The microservices architecture includes:

**Infrastructure Services:**
- PostgreSQL Database (port 5432)
- Keycloak Authentication (port 8080)

**Core Services:**
- Config Server (port 8888)
- Eureka Server (port 8761)
- API Gateway (port 9001)

**Business Services:**
- Accounts Service (port 8091)
- Cash Service (port 8092)
- Exchange Service (port 8093)
- Transfer Service (port 8094)
- Blocker Service (port 8095)
- Notifications Service (port 8096)
- Exchange Generator Service (port 8097)
- Front UI (port 8090)

## 🚀 Quick Start

### Option 1: Full Docker Setup (Recommended)

Start all services with proper dependencies:

```bash
# Build and start all services
docker-compose -f docker-compose-full.yml up --build

# Or run in background
docker-compose -f docker-compose-full.yml up -d --build
```

### Option 2: Infrastructure Only (for Local Development)

If you want to run some services locally but use Docker for infrastructure:

```bash
# Start only PostgreSQL and Keycloak
docker-compose up -d

# Then run your services locally using Maven
mvn spring-boot:run -pl config-server
mvn spring-boot:run -pl eureka-server
# ... etc
```

## 🔧 Service Startup Order

The Docker Compose configuration automatically handles startup order with `depends_on`:

1. **Infrastructure**: PostgreSQL & Keycloak (parallel)
2. **Core**: Config Server → Eureka Server
3. **Gateway**: API Gateway
4. **Business Services**: All business services (parallel start)
5. **Frontend**: Front UI

## 🛡️ Root vs Non-Root User

These Docker containers run as **root user** for simplicity:

### ✅ **Advantages of Root User:**
- **Simpler Dockerfile** - No user management complexity
- **Easier debugging** - Can install packages at runtime
- **No permission issues** - Can write anywhere needed
- **Faster builds** - No ownership changes needed
- **Development friendly** - Easy troubleshooting

### ⚠️ **Security Considerations:**
- **Development use** - Perfect for development environments
- **Production** - Consider non-root users for production
- **Container isolation** - Still isolated from host system
- **Network security** - Use proper firewall rules

## 📊 Monitoring Startup

Monitor service startup progress:

```bash
# Watch service status
docker-compose -f docker-compose-full.yml ps

# Follow logs for all services
docker-compose -f docker-compose-full.yml logs -f

# Follow logs for specific service
docker-compose -f docker-compose-full.yml logs -f accounts-service
```

## 🌐 Service URLs

Once all services are running:

- **Frontend**: http://localhost:8090
- **API Gateway**: http://localhost:9001
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **PostgreSQL**: localhost:5432 (postgres/1)

## 🔍 Individual Service Management

### Build specific service:
```bash
docker build -t bank/config-server -f config-server/Dockerfile .
docker build -t bank/accounts-service -f accounts-service/Dockerfile .
```

### Run specific service:
```bash
docker run -p 8888:8888 --name config-server bank/config-server
```

### Start specific services only:
```bash
# Start only infrastructure
docker-compose -f docker-compose-full.yml up -d postgres keycloak

# Start core services
docker-compose -f docker-compose-full.yml up -d config-server eureka-server

# Start specific business service
docker-compose -f docker-compose-full.yml up -d accounts-service
```

## 🛠️ Environment Configuration

### Memory Settings
Each service is configured with:
- **Heap Size**: 512MB max, 256MB initial
- **Lightweight**: No additional security layers

### Environment Variables
Key environment variables you can override:

```yaml
# Database Configuration
DATABASE_URL: jdbc:postgresql://postgres:5432/bank
DATABASE_USERNAME: postgres
DATABASE_PASSWORD: 1

# Service Discovery
CONFIG_SERVER_URL: http://config-server:8888
EUREKA_SERVER_URL: http://eureka-server:8761/eureka

# JVM Options
JAVA_OPTS: "-Xmx512m -Xms256m"
```

## 🛑 Stopping Services

```bash
# Stop all services
docker-compose -f docker-compose-full.yml down

# Stop and remove volumes (⚠️ deletes database data)
docker-compose -f docker-compose-full.yml down -v

# Stop specific service
docker-compose -f docker-compose-full.yml stop accounts-service
```

## 🔧 Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8080-8097, 9001, 5432 are available
2. **Memory issues**: Increase Docker Desktop memory limit to 6GB+
3. **Startup failures**: Check logs with `docker-compose logs [service-name]`

### Debug Mode
Since containers run as root, you can easily debug:

```bash
# Enter container for debugging
docker exec -it accounts-service bash

# Install debugging tools at runtime
docker exec -it accounts-service apt-get update && apt-get install -y net-tools

# Enable debug logging
docker-compose -f docker-compose-full.yml run -e LOGGING_LEVEL_ROOT=DEBUG accounts-service
```

## 🔄 Development Workflow

### Hot Reload Development
For development with hot reload:

1. Start infrastructure only:
   ```bash
   docker-compose up -d postgres keycloak
   ```

2. Run services locally:
   ```bash
   mvn spring-boot:run -pl config-server
   ```

### Adding Security Later

If you want to add non-root users later, modify the Dockerfile:

```dockerfile
# Add before CMD
RUN groupadd -r spring && useradd -r -g spring spring
RUN chown spring:spring app.jar
USER spring
```

## 📝 Simple Dockerfile Template

Each service uses this simple pattern:

```dockerfile
# Multi-stage build
FROM maven:3.9.5-openjdk-21-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY common/ ./common/
COPY [service]/pom.xml ./[service]/
RUN mvn dependency:go-offline -pl [service]
COPY [service]/src ./[service]/src
RUN mvn clean package -pl [service] -DskipTests

# Runtime
FROM openjdk:21-jre-slim
WORKDIR /app
COPY --from=builder /app/[service]/target/*.jar app.jar
EXPOSE [PORT]
ENV JAVA_OPTS=""
CMD java $JAVA_OPTS -jar app.jar
```

## 🎯 Summary

This simplified Docker setup provides:
- ✅ **Easy to understand** - Simple Dockerfiles
- ✅ **Fast development** - Root user for debugging
- ✅ **Proper startup order** - Dependencies handled by Docker Compose
- ✅ **Production ready** - Can be hardened later
- ✅ **Developer friendly** - No permission hassles

Perfect for development and testing your Bank microservices! 