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
- **Eureka Server** (port 8761) - Service Discovery i registracija servisa
- **Config Server** (port 8888) - Centralizovana konfiguracija
- **API Gateway** (port 8080) - Routing i load balancing

### Poslovni Servisi
- **Product Service** (port 8081) - ✅ Upravljanje proizvodima (CRUD operacije)
- **Order Service** (port 8082) - 🚧 Upravljanje porudžbinama
- **User Service** (port 8083) - 🚧 Upravljanje korisnicima
- **Inventory Service** (port 8084) - 🚧 Upravljanje zalihama
- **Payment Service** (port 8085) - 🚧 Obrada plaćanja
- **Notification Service** (port 8086) - 🚧 Slanje obaveštenja

## Product Service API

Product Service pruža REST API za upravljanje proizvodima:

### Endpoints
- `POST /api/products` - Kreiranje novog proizvoda
- `GET /api/products` - Lista svih proizvoda
  - Query parametri: `?category=Electronics`, `?search=keyword`, `?activeOnly=true`
- `GET /api/products/{id}` - Proizvod po ID-u
- `GET /api/products/sku/{sku}` - Proizvod po SKU-u
- `PUT /api/products/{id}` - Ažuriranje proizvoda
- `PATCH /api/products/{id}/stock?quantity=50` - Ažuriranje zaliha
- `DELETE /api/products/{id}` - Brisanje proizvoda

### Primer Request Body
```json
{
  "sku": "LAPTOP-001",
  "name": "Gaming Laptop",
  "description": "High-performance gaming laptop",
  "price": 1299.99,
  "stockQuantity": 50,
  "category": "Electronics",
  "imageUrl": "http://example.com/laptop.jpg",
  "active": true
}
```

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