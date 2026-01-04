# RxBuddy Application - How to Run

## Prerequisites

- **Java 17** (JDK)
- **Maven 3.8+**
- **Node.js 18+** and npm
- **MySQL 8.0** (optional - H2 in-memory database available for dev mode)

## Backend (Microservices)

### Quick Start (Development Mode with H2 Database)

This mode uses H2 in-memory database, no MySQL required.

#### 1. Install Dependencies

```bash
cd rxbuddy-microservices

# Install parent POM
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn install -N -DskipTests

# Install common modules
cd common/common-dto && mvn install -DskipTests && cd ../..
cd common/common-security && mvn install -DskipTests && cd ../..
cd common/common-utils && mvn install -DskipTests && cd ../..
```

#### 2. Start Services (in order)

**Terminal 1 - Discovery Server (Eureka):**
```bash
cd infrastructure/discovery-server
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn spring-boot:run -DskipTests
```
Wait until you see "Started DiscoveryServerApplication" (usually 20-30 seconds)

**Terminal 2 - API Gateway:**
```bash
cd infrastructure/api-gateway
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn spring-boot:run -DskipTests
```

**Terminal 3 - Auth Service:**
```bash
cd services/auth-service
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn spring-boot:run -DskipTests
```

**Terminal 4 - User Service (with dev profile for H2):**
```bash
cd services/user-service
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn spring-boot:run -DskipTests -Dspring-boot.run.profiles=dev
```

**Terminal 5 - Tenant Service (with dev profile for H2):**
```bash
cd services/tenant-service
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn spring-boot:run -DskipTests -Dspring-boot.run.profiles=dev
```

**Terminal 6 - Card Service (with dev profile for H2):**
```bash
cd services/card-service
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn spring-boot:run -DskipTests -Dspring-boot.run.profiles=dev
```

### Service Ports

| Service | Port | URL |
|---------|------|-----|
| Discovery Server (Eureka) | 8761 | http://localhost:8761 |
| API Gateway | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |
| User Service | 8083 | http://localhost:8083 |
| Tenant Service | 8084 | http://localhost:8084 |
| Card Service | 8089 | http://localhost:8089 |

### Verify Services

1. Open Eureka Dashboard: http://localhost:8761
2. You should see all services registered: API-GATEWAY, AUTH-SERVICE, USER-SERVICE, TENANT-SERVICE, CARD-SERVICE

---

## Frontend

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

Frontend will be available at: **http://localhost:5173**

### 3. Build for Production

```bash
npm run build
```

---

## Login Credentials

| Field | Value |
|-------|-------|
| **Phone** | `9999999999` |
| **Password** | `Admin@123` |

---

## Production Mode (with MySQL)

### 1. Start MySQL

Using Docker:
```bash
docker-compose up -d mysql
```

Or ensure MySQL is running on port 3306/3307 with these databases:
- rxbuddy_user
- rxbuddy_tenant
- rxbuddy_card
- rxbuddy_auth

### 2. Start Services without dev profile

```bash
# User Service (uses MySQL)
cd services/user-service
mvn spring-boot:run -DskipTests

# Tenant Service (uses MySQL)
cd services/tenant-service
mvn spring-boot:run -DskipTests

# Card Service (uses MySQL)
cd services/card-service
mvn spring-boot:run -DskipTests
```

---

## Environment Variables

### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| MYSQL_HOST | localhost | MySQL host |
| MYSQL_PORT | 3306 | MySQL port |
| MYSQL_USER | rxbuddy | MySQL username |
| MYSQL_PASSWORD | rxbuddypassword | MySQL password |
| EUREKA_URI | http://localhost:8761/eureka | Eureka server URL |
| JWT_SECRET | (default in config) | JWT signing secret |

### Frontend

Create `.env` file in frontend directory:
```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

---

## Troubleshooting

### Port already in use
```bash
# Find and kill process on port (e.g., 8080)
lsof -ti:8080 | xargs kill -9
```

### Maven dependency issues
```bash
# Clean and reinstall
mvn clean install -DskipTests
```

### Services not registering with Eureka
- Ensure Discovery Server is running first
- Wait 30 seconds for services to register
- Check Eureka dashboard at http://localhost:8761

### Frontend API errors
- Ensure API Gateway is running on port 8080
- Check browser console for CORS errors
- Verify backend services are registered in Eureka
