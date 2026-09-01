# PayRoute Development Makefile

.PHONY: dev-backend dev-frontend build-backend install docker-up docker-down

## Start backend (requires PostgreSQL running)
dev-backend:
	cd backend && mvn spring-boot:run

## Start frontend dev server
dev-frontend:
	cd frontend && npm run dev

## Build backend JAR
build-backend:
	cd backend && mvn clean package -DskipTests

## Install frontend dependencies
install:
	cd frontend && npm install

## Start all services with Docker Compose
docker-up:
	docker-compose up -d

## Stop all Docker services
docker-down:
	docker-compose down

## View logs
logs:
	docker-compose logs -f

## Audit frontend dependencies for vulnerabilities
audit:
	cd frontend && npm audit
