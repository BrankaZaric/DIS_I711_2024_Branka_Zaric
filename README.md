# E-Commerce Microservices System

Mikroservisni sistem za e-commerce razvijen korišćenjem Spring Cloud framework-a.

## 📋 Sadržaj

- [Pregled Sistema](#pregled-sistema)
- [Arhitektura](#arhitektura)
- [Servisi](#servisi)
- [Tehnologije](#tehnologije)
- [Pokretanje Sistema](#pokretanje-sistema)

## Pregled Sistema

Sistem se sastoji od više nezavisnih mikroservisa koji komuniciraju međusobno kako sinhrono (REST API) tako i asinhrono (message broker).

## Arhitektura

```
┌─────────────────┐
│   API Gateway   │  ← Jedinstvena ulazna tačka
└────────┬────────┘
         │
    ┌────┴─────┐
    │  Eureka  │  ← Service Discovery
    └────┬─────┘
         │
    ┌────┴────────────────────────┐
    │                             │
┌───▼───────┐              ┌─────▼──────┐
│ Product   │              │  Order     │
│ Service   │◄────────────►│  Service   │
└───────────┘              └────────────┘
```

## Servisi

### Infrastrukturni Servisi
- **Eureka Server** - Service Discovery i registracija servisa
- **Config Server** - Centralizovana konfiguracija
- **API Gateway** - Routing i load balancing

### Poslovni Servisi
- **Product Service** - Upravljanje proizvodima
- **Order Service** - Upravljanje porudžbinama
- **User Service** - Upravljanje korisnicima
- **Inventory Service** - Upravljanje zalihama
- **Payment Service** - Obrada plaćanja
- **Notification Service** - Slanje obaveštenja

## Tehnologije

- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- PostgreSQL
- Kafka/RabbitMQ
- Docker & Docker Compose
- Maven

## Pokretanje Sistema

```bash
# Build all services
mvn clean install

# Start with Docker Compose
docker-compose up -d
```

## Status Projekta

🚧 **U razvoju** - Faza 1: Setup infrastrukture