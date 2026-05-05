.PHONY: help build test clean package docker-build docker-up docker-down run-dev coverage

# Colors for output
GREEN  := $(shell tput -Txterm setaf 2)
YELLOW := $(shell tput -Txterm setaf 3)
WHITE  := $(shell tput -Txterm setaf 7)
RESET  := $(shell tput -Txterm sgr0)

help: ## Show this help message
	@echo ''
	@echo 'Usage:'
	@echo '  ${YELLOW}make${RESET} ${GREEN}<target>${RESET}'
	@echo ''
	@echo 'Targets:'
	@awk 'BEGIN {FS = ":.*?## "} { \
		if (/^[a-zA-Z_-]+:.*?##.*$$/) {printf "    ${YELLOW}%-20s${GREEN}%s${RESET}\n", $$1, $$2} \
		else if (/^## .*$$/) {printf "  ${WHITE}%s${RESET}\n", substr($$1,4)} \
		}' $(MAKEFILE_LIST)

## Build Commands
build: ## Compile all services
	@echo "${GREEN}Building all services...${RESET}"
	mvn clean compile -DskipTests

package: ## Build JAR files for all services
	@echo "${GREEN}Packaging all services...${RESET}"
	mvn clean package -DskipTests

install: ## Install all services to local Maven repository
	@echo "${GREEN}Installing all services...${RESET}"
	mvn clean install -DskipTests

## Test Commands
test: ## Run all tests
	@echo "${GREEN}Running all tests...${RESET}"
	mvn clean test

test-product: ## Run Product Service tests only
	@echo "${GREEN}Running Product Service tests...${RESET}"
	cd product-service && mvn test

test-order: ## Run Order Service tests only
	@echo "${GREEN}Running Order Service tests...${RESET}"
	cd order-service && mvn test

coverage: ## Generate code coverage report
	@echo "${GREEN}Generating coverage reports...${RESET}"
	mvn clean test jacoco:report
	@echo "${YELLOW}Coverage reports generated in target/site/jacoco/${RESET}"

integration-test: ## Run integration tests
	@echo "${GREEN}Running integration tests...${RESET}"
	mvn verify -Pintegration-tests

## Docker Commands
docker-build: package ## Build Docker images for all services
	@echo "${GREEN}Building Docker images...${RESET}"
	docker-compose build

docker-up: ## Start all services with Docker Compose
	@echo "${GREEN}Starting services with Docker Compose...${RESET}"
	docker-compose up -d

docker-down: ## Stop all Docker containers
	@echo "${GREEN}Stopping Docker containers...${RESET}"
	docker-compose down

docker-logs: ## Show logs from all containers
	docker-compose logs -f

docker-ps: ## Show running containers
	docker-compose ps

## Development Commands
run-eureka: ## Run Eureka Server locally
	@echo "${GREEN}Starting Eureka Server...${RESET}"
	cd eureka-server && mvn spring-boot:run

run-config: ## Run Config Server locally
	@echo "${GREEN}Starting Config Server...${RESET}"
	cd config-server && mvn spring-boot:run

run-gateway: ## Run API Gateway locally
	@echo "${GREEN}Starting API Gateway...${RESET}"
	cd api-gateway && mvn spring-boot:run

run-product: ## Run Product Service locally
	@echo "${GREEN}Starting Product Service...${RESET}"
	cd product-service && mvn spring-boot:run

run-order: ## Run Order Service locally
	@echo "${GREEN}Starting Order Service...${RESET}"
	cd order-service && mvn spring-boot:run

## Database Commands
db-start: ## Start PostgreSQL databases with Docker
	@echo "${GREEN}Starting databases...${RESET}"
	docker-compose up -d postgres-product postgres-order

db-stop: ## Stop PostgreSQL databases
	@echo "${GREEN}Stopping databases...${RESET}"
	docker-compose stop postgres-product postgres-order

db-clean: ## Remove database volumes
	@echo "${YELLOW}Removing database volumes...${RESET}"
	docker-compose down -v

## Cleanup Commands
clean: ## Clean all build artifacts
	@echo "${GREEN}Cleaning build artifacts...${RESET}"
	mvn clean

clean-all: clean docker-down db-clean ## Clean everything including Docker volumes
	@echo "${GREEN}Cleaned all artifacts and Docker resources${RESET}"

## Quick Start
quick-start: docker-build docker-up ## Build and start all services
	@echo "${GREEN}All services started!${RESET}"
	@echo "${YELLOW}Eureka Dashboard: http://localhost:8761${RESET}"
	@echo "${YELLOW}API Gateway: http://localhost:8080${RESET}"

## Status Check
status: ## Check status of all services
	@echo "${GREEN}Service Status:${RESET}"
	@echo "${YELLOW}Eureka Server:${RESET} http://localhost:8761"
	@echo "${YELLOW}Config Server:${RESET} http://localhost:8888"
	@echo "${YELLOW}API Gateway:${RESET} http://localhost:8080"
	@echo "${YELLOW}Product Service:${RESET} http://localhost:8081/actuator/health"
	@echo "${YELLOW}Order Service:${RESET} http://localhost:8082/actuator/health"
