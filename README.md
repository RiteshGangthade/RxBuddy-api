# RXBuddy Microservices

Multi-tenant Pharmacy Management System built with Spring Cloud Microservices Architecture.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (:8080)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Auth Service │    │ User Service │    │ Tenant Svc   │
│   (:8081)    │    │   (:8083)    │    │   (:8082)    │
└──────────────┘    └──────────────┘    └──────────────┘
        │                     │                     │
        └─────────────────────┴─────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│               Infrastructure Layer                               │
├──────────────┬──────────────┬──────────────┬───────────────────┤
│   Eureka     │   Config     │  RabbitMQ    │      MySQL        │
│   (:8761)    │   (:8888)    │   (:5672)    │     (:3306)       │
└──────────────┴──────────────┴──────────────┴───────────────────┘
```

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

## Quick Start

### 1. Start Infrastructure

```bash
# Start MySQL, RabbitMQ, Redis, Zipkin
make infra-up

# Or with phpMyAdmin for database management
make infra-dev
```

### 2. Build All Modules

```bash
make build
```

### 3. Run Services (Development)

Open separate terminals for each service:

```bash
# Terminal 1: Discovery Server (must start first)
make run-discovery

# Terminal 2: Config Server
make run-config

# Terminal 3: API Gateway
make run-gateway

# Terminal 4: User Service
make run-user

# Terminal 5: Auth Service
make run-auth
```

### 4. Access Services

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| phpMyAdmin | http://localhost:8081 |
| Zipkin | http://localhost:9411 |
| RabbitMQ | http://localhost:15672 |

## API Endpoints

### Authentication

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone": "9999999999", "password": "Admin@123"}'

# Response
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "name": "Super Admin",
      "role": "SUPER_ADMIN"
    }
  }
}
```

## Project Structure

```
rxbuddy-microservices/
├── common/                     # Shared libraries
│   ├── common-dto/             # Shared DTOs
│   ├── common-security/        # JWT utilities
│   ├── common-messaging/       # Event classes
│   └── common-utils/           # Utilities
├── infrastructure/             # Infrastructure services
│   ├── config-server/          # Centralized config
│   ├── discovery-server/       # Eureka
│   └── api-gateway/            # Spring Cloud Gateway
├── services/                   # Business services
│   ├── auth-service/           # Authentication
│   ├── user-service/           # User management
│   ├── tenant-service/         # Tenant management
│   ├── inventory-service/      # Products & stock
│   ├── billing-service/        # Bills & payments
│   ├── customer-service/       # Customers & wallet
│   ├── doctor-service/         # Doctors & commissions
│   ├── supplier-service/       # Suppliers & ledger
│   └── notification-service/   # Email & SMS
├── config-repo/                # Configuration files
├── docker/                     # Docker configs
└── docker-compose.yml
```

## Default Credentials

| Service | Username | Password |
|---------|----------|----------|
| Super Admin | 9999999999 | Admin@123 |
| MySQL | rxbuddy | rxbuddypassword |
| RabbitMQ | rxbuddy | rxbuddypassword |

## Development

### Adding a New Service

1. Create directory under `services/`
2. Add module to parent `pom.xml`
3. Create POM with common dependencies
4. Add Dockerfile
5. Add config in `config-repo/`
6. Add route in API Gateway config

### Running Tests

```bash
# All tests
./mvnw test

# Specific service
cd services/auth-service && ../../mvnw test
```

## License

MIT
