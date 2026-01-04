# RXBuddy Microservices Makefile

.PHONY: help build clean infra-up infra-down full-up full-down logs

# Default target
help:
	@echo "RXBuddy Microservices - Development Commands"
	@echo "============================================="
	@echo ""
	@echo "Infrastructure:"
	@echo "  make infra-up      - Start infrastructure (MySQL, RabbitMQ, Redis, Zipkin)"
	@echo "  make infra-down    - Stop infrastructure"
	@echo "  make infra-dev     - Start infrastructure + phpMyAdmin"
	@echo ""
	@echo "Build:"
	@echo "  make build         - Build all modules"
	@echo "  make clean         - Clean all modules"
	@echo ""
	@echo "Run Services (Local):"
	@echo "  make run-config    - Run Config Server"
	@echo "  make run-discovery - Run Discovery Server"
	@echo "  make run-gateway   - Run API Gateway"
	@echo "  make run-auth      - Run Auth Service"
	@echo "  make run-user      - Run User Service"
	@echo "  make run-tenant    - Run Tenant Service"
	@echo "  make run-card      - Run Card Service"
	@echo ""
	@echo "Docker:"
	@echo "  make full-up       - Start all services with Docker"
	@echo "  make full-down     - Stop all Docker services"
	@echo "  make logs          - View all logs"
	@echo ""
	@echo "Database:"
	@echo "  make db-shell      - Open MySQL shell"

# Build all modules
build:
	./mvnw clean package -DskipTests

# Clean all modules
clean:
	./mvnw clean

# Start infrastructure only (MySQL only - minimal)
infra-up:
	docker-compose up -d mysql

# Start infrastructure with phpMyAdmin (for development)
infra-dev:
	docker-compose --profile dev up -d mysql phpmyadmin

# Start with messaging (RabbitMQ) - Phase 4
infra-messaging:
	docker-compose --profile messaging up -d mysql rabbitmq

# Start with all optional services
infra-full:
	docker-compose --profile dev --profile messaging --profile caching --profile tracing up -d

# Stop infrastructure
infra-down:
	docker-compose down

# Start all services
full-up:
	docker-compose --profile full up -d --build

# Stop all services
full-down:
	docker-compose --profile full down

# View logs
logs:
	docker-compose logs -f

# Database shell
db-shell:
	docker-compose exec mysql mysql -u rxbuddy -prxbuddypassword

# Run individual services locally
run-config:
	cd infrastructure/config-server && ../../mvnw spring-boot:run

run-discovery:
	cd infrastructure/discovery-server && ../../mvnw spring-boot:run

run-gateway:
	cd infrastructure/api-gateway && ../../mvnw spring-boot:run

run-auth:
	cd services/auth-service && ../../mvnw spring-boot:run

run-user:
	cd services/user-service && ../../mvnw spring-boot:run

run-tenant:
	cd services/tenant-service && ../../mvnw spring-boot:run

run-card:
	cd services/card-service && ../../mvnw spring-boot:run
