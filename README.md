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
│   API Gateway   │  ← Jedinstvena ulazna tačka (port 8080)
└────────┬────────┘
         │
    ┌────┴─────┐
    │  Eureka  │  ← Service Discovery (port 8761)
    └────┬─────┘
         │
    ┌────┴─────┐
    │  Config  │  ← Centralizovana konfiguracija (port 8888)
    └──────────┘

┌──────────────┐     Sinhrona REST      ┌─────────────┐
│   Product    │◄────(OpenFeign)────────┤   Order     │
│   Service    │                        │   Service   │
│              │   GET /api/products/   │             │
│ (port 8081)  │      sku/{sku}         │ (port 8082) │
│              │                        │             │
│  PostgreSQL  │                        │  PostgreSQL │
│  (port 5432) │                        │  (port 5433)│
└──────────────┘                        └─────────────┘
```

## Servisi

### Infrastrukturni Servisi
- **Eureka Server** (port 8761) - Service Discovery i registracija servisa
- **Config Server** (port 8888) - Centralizovana konfiguracija
- **API Gateway** (port 8080) - Routing i load balancing

### Poslovni Servisi
- **Product Service** (port 8081) - ✅ Upravljanje proizvodima (CRUD operacije)
- **Order Service** (port 8082) - ✅ Upravljanje porudžbinama (sinhrona komunikacija sa Product Service)
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

## Order Service API

Order Service upravlja porudžbinama i komunicira sa Product Service-om putem OpenFeign klijenta.

### Sinhrona Komunikacija
Order Service koristi **Spring Cloud OpenFeign** za REST pozive prema Product Service-u:
- Validacija dostupnosti proizvoda
- Provera stanja zaliha
- Preuzimanje cena i detalja proizvoda

### Endpoints
- `POST /api/orders` - Kreiranje nove porudžbine
- `GET /api/orders` - Lista svih porudžbina
  - Query parametri: `?customerEmail=test@test.com`, `?status=PENDING`
- `GET /api/orders/{id}` - Porudžbina po ID-u
- `GET /api/orders/number/{orderNumber}` - Porudžbina po broju porudžbine
- `PATCH /api/orders/{id}/status?status=CONFIRMED` - Ažuriranje statusa
- `DELETE /api/orders/{id}` - Otkazivanje porudžbine

### Order Status Flow
`PENDING` → `CONFIRMED` → `PROCESSING` → `SHIPPED` → `DELIVERED`
(ili `CANCELLED` u bilo kom momentu pre DELIVERED)

### Primer Request Body
```json
{
  "customerEmail": "john.doe@example.com",
  "customerName": "John Doe",
  "shippingAddress": "123 Main St, New York, NY 10001",
  "items": [
    {
      "productSku": "LAPTOP-001",
      "quantity": 2
    }
  ]
}
```

### Validacija
- Proizvodi moraju da postoje u Product Service-u
- Proizvodi moraju biti aktivni (active=true)
- Dovoljno stanja zaliha za traženu količinu
- Automatsko generisanje jedinstvenog broja porudžbine (ORD-XXXXXXXX)
- Automatski izračunata ukupna cena

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

## Napomene

Sistem koristi **Database per Service** pattern - svaki mikroservis ima svoju nezavisnu PostgreSQL bazu podataka, što omogućava nezavisno skaliranje i deploy.

Komunikacija između servisa je realizovana putem **Spring Cloud OpenFeign** klijenta koji pruža deklarativni REST klijent sa automatskim load balancing-om i service discovery integracijom.