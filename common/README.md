# Common Module

## Description
The Common module is a shared library that contains all the common components used across the Bank microservices architecture. It provides a centralized location for data models, DTOs, repositories, and mappers that are shared between multiple services.

### Key Features:
- Shared JPA entities and data models
- Data Transfer Objects (DTOs) for service communication
- Spring Data JPA repositories
- MapStruct mappers for entity-DTO conversion
- Centralized database schema definitions
- Reusable components across all microservices

## Module Structure

### 📁 Package Organization
```
com.cherniva.common/
├── dto/          # Data Transfer Objects
├── mapper/       # MapStruct mappers
├── model/        # JPA entities
└── repo/         # Spring Data repositories
```

## Components

### 🗃️ Models (JPA Entities)
Located in `com.cherniva.common.model`

| Entity | Description | Key Fields |
|--------|-------------|------------|
| **UserDetails** | User account information | id, username, password, name, surname, birthdate, accounts |
| **Account** | Bank account entity | id, userDetails, currency, amount, active |
| **Currency** | Supported currencies | id, code, name |
| **ExchangeRate** | Currency exchange rates | id, sourceCurrency, targetCurrency, sellPrice, buyPrice |

### 📋 DTOs (Data Transfer Objects)
Located in `com.cherniva.common.dto`

| DTO | Purpose | Used By |
|-----|---------|---------|
| **UserRegistrationDto** | User registration data | Accounts Service |
| **UserLoginDto** | User login credentials | Accounts Service, Front UI |
| **UserAccountResponseDto** | Complete user account info | All services |
| **AccountDto** | Account information transfer | All services |
| **TransferDto** | Transfer operation data | Transfer Service, Cash Service |
| **SessionValidationDto** | Session validation response | All services |
| **ExchangeRateDto** | Exchange rate information | Exchange Service, Transfer Service |
| **ExchangeRatesResponseDto** | Exchange rates API response | Exchange Service |
| **NotificationDto** | General notification data | Notifications Service |
| **AccountsNotificationDto** | Accounts operation notifications | Accounts Service |
| **CashNotificationDto** | Cash operation notifications | Cash Service |
| **TransferNotificationDto** | Transfer operation notifications | Transfer Service |

### 🔄 Mappers (MapStruct)
Located in `com.cherniva.common.mapper`

| Mapper | Purpose | Features |
|--------|---------|----------|
| **UserMapper** | User entity ↔ DTO conversion | Name capitalization, collection mapping |
| **AccountMapper** | Account entity ↔ DTO conversion | Currency code mapping, nested object handling |

### 🗄️ Repositories (Spring Data JPA)
Located in `com.cherniva.common.repo`

| Repository | Entity | Additional Methods |
|------------|--------|-------------------|
| **UserDetailsRepo** | UserDetails | findByUsername, existsByUsername |
| **AccountRepo** | Account | Standard JPA operations |
| **CurrencyRepo** | Currency | findCurrencyByCode |
| **ExchangeRateRepo** | ExchangeRate | Standard JPA operations |

## Dependencies

### Core Dependencies
- **Spring Boot Starter Data JPA** - Database access and ORM
- **Lombok** - Boilerplate code reduction
- **MapStruct** - Bean mapping
- **Jakarta Persistence API** - JPA annotations

### Testing Dependencies
- **JUnit Jupiter** - Unit testing framework
- **Spring Boot Test** - Integration testing
- **H2 Database** - In-memory database for testing

## Database Schema

### Relationships
- **UserDetails** → **Account** (One-to-Many)
- **Account** → **Currency** (Many-to-One)
- **ExchangeRate** → **Currency** (Many-to-One for source and target)

### Key Constraints
- Users can have multiple accounts in different currencies
- Accounts are linked to a specific currency
- Exchange rates define conversion between currency pairs

## Usage in Services

### Including Common Module
```xml
<dependency>
    <groupId>com.cherniva</groupId>
    <artifactId>common</artifactId>
</dependency>
```

### Configuration Required
Services using the common module must include:

```java
@EntityScan(basePackages = {"com.cherniva.common.model"})
@EnableJpaRepositories(basePackages = {"com.cherniva.common.repo"})
```

### Example Usage
```java
// Using repositories
@Autowired
private UserDetailsRepo userDetailsRepo;

// Using mappers
@Autowired
private UserMapper userMapper;

// Converting entities to DTOs
UserAccountResponseDto dto = userMapper.userToUserAccountResponse(user);
```

## Testing

### Test Configuration
The module includes `CommonTestConfiguration` for testing with:
- H2 in-memory database
- Test-specific profiles
- Repository layer testing support

### Running Tests
```bash
cd common
mvn test
```

## Notes
- This module is a **dependency** for all other services
- Must be built **first** before other services
- Contains no main application class (library module)
- Provides the foundation for data consistency across services
- Uses MapStruct for high-performance bean mapping
- Includes comprehensive test coverage for repository layer 