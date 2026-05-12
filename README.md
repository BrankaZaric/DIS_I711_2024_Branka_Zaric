# E-Commerce Microservices System

Mikroservisni sistem za e-commerce razvijen korišćenjem Spring Cloud framework-a.

## Pregled Sistema

Sistem se sastoji od više nezavisnih mikroservisa koji komuniciraju međusobno kako sinhrono (REST API) tako i asinhrono (message broker).

## Arhitektura

```mermaid
graph TB
    Client[👤 Client/Browser]
    
    Client -->|HTTP Requests| Gateway
    
    subgraph infra[" INFRASTRUKTURA "]
        Gateway[🌐 API Gateway<br/>:8080]
        Eureka[📋 Service Discovery<br/>Eureka :8761]
        Config[⚙️ Config Server<br/>:8888]
    end
    
    Gateway -->|Routes| Services
    
    subgraph Services[" BUSINESS SERVISI "]
        direction LR
        Product[📦 Product<br/>:8081]
        Order[🛒 Order<br/>:8082]
        User[👥 User<br/>:8083]
        Inventory[📊 Inventory<br/>:8084]
        Payment[💳 Payment<br/>:8085]
        Notification[📧 Notification<br/>:8086]
    end
    
    Order -.->|OpenFeign REST| Product
    Order -->|Publish Event| RabbitMQ
    RabbitMQ -->|Consume Event| Notification
    
    RabbitMQ[🐰 RabbitMQ :5672]
    
    Product --> ProductDB
    Order --> OrderDB
    User --> UserDB
    Inventory --> InventoryDB
    Payment --> PaymentDB
    Notification --> NotificationDB
    
    subgraph databases[" DATABASES (PostgreSQL) "]
        direction LR
        ProductDB[(productdb<br/>:5432)]
        OrderDB[(orderdb<br/>:5433)]
        UserDB[(userdb<br/>:5434)]
        InventoryDB[(inventorydb<br/>:5435)]
        PaymentDB[(paymentdb<br/>:5436)]
        NotificationDB[(notificationdb<br/>:5437)]
    end
    
    Services -.->|Register| Eureka
    
    style infra fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    style Services fill:#fff8e1,stroke:#f57c00,stroke-width:2px
    style databases fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    style Gateway fill:#bbdefb
    style Eureka fill:#bbdefb
    style Config fill:#bbdefb
    style RabbitMQ fill:#ffcdd2
    style Client fill:#c8e6c9
```

**Ključne Komunikacije:**
- **Sinhrona**: Order Service ↔ Product Service 
- **Asinhrona**: Order Service → RabbitMQ → Notification Service
- **Discovery**: Svi servisi se registruju na Eureka Server
- **Pattern**: Database per Service (svaki servis ima svoju bazu)

## Servisi

### Infrastrukturni Servisi
- **Eureka Server** (port 8761) - Service Discovery i registracija servisa
- **Config Server** (port 8888) - Centralizovana konfiguracija
- **API Gateway** (port 8080) - Routing i load balancing

### Poslovni Servisi
- **Product Service** (port 8081) - Upravljanje proizvodima (CRUD operacije)
- **Order Service** (port 8082) - Upravljanje porudžbinama sa sinhronom komunikacijom (OpenFeign)
- **User Service** (port 8083) - Autentifikacija korisnika (JWT, role-based access)
- **Inventory Service** (port 8084) - Upravljanje zalihama
- **Payment Service** (port 8085) - Obrada plaćanja
- **Notification Service** (port 8086) - Slanje obaveštenja (asinhrona komunikacija preko RabbitMQ)

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

## User Service API

User Service upravlja korisnicima i pruža JWT autentifikaciju sa role-based access control.

### Sigurnost
- **JWT Token** autentifikacija
- **BCrypt** hashing lozinki
- **Role-based access** (USER, ADMIN)
- Session stateless (ne koristi server-side sessions)

### Endpoints

#### Javni (bez autentifikacije)
- `POST /api/users/register` - Registracija novog korisnika
- `POST /api/users/login` - Login i dobijanje JWT tokena

#### Zaštićeni (zahtevaju JWT token)
- `GET /api/users/{id}` - Detalji korisnika po ID-u (USER, ADMIN)
- `GET /api/users/email/{email}` - Korisnik po email-u (USER, ADMIN)
- `PUT /api/users/{id}` - Ažuriranje korisnika (USER, ADMIN)

#### Admin Only
- `GET /api/users` - Lista svih korisnika (ADMIN)
  - Query parametri: `?activeOnly=true`
- `PATCH /api/users/{id}/deactivate` - Deaktivacija korisnika (ADMIN)
- `PATCH /api/users/{id}/activate` - Aktivacija korisnika (ADMIN)
- `DELETE /api/users/{id}` - Brisanje korisnika (ADMIN)
- `PATCH /api/users/admin/promote/{id}` - Promocija u ADMIN (ADMIN)

### Primer Registration Request
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepass123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "123-456-7890"
}
```

### Primer Login Request/Response
Request:
```json
{
  "usernameOrEmail": "johndoe",
  "password": "securepass123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER"
}
```

### Korišćenje JWT Tokena
Nakon uspešnog login-a, JWT token se prosleđuje u svim zahtevima:
```
Authorization: Bearer <jwt-token>
```

## Inventory Service API

- `POST /api/inventory` - Kreiranje novog Inventory zapisa
- `GET /api/inventory/product/{productId}` - Dobijanje zaliha za proizvod
- `PATCH /api/inventory/{id}/adjust?quantity=10` - Ažuriranje količine

## Payment Service API

- `POST /api/payments` - Kreiranje novog plaćanja
- `GET /api/payments/{id}` - Detalji plaćanja
- `GET /api/payments/order/{orderId}` - Plaćanja za porudžbinu

## Notification Service API

- `GET /api/notifications/user/{userId}` - Sve notifikacije za korisnika
- RabbitMQ Consumer: prima događaje o porudžbinama i automatski kreira notifikacije

## Komunikacija između servisa

### Sinhrona komunikacija (REST/OpenFeign)
- Order Service → Product Service (validacija proizvoda, provera zaliha)
- Implementirano kroz Spring Cloud OpenFeign klijenta
- Automatski load balancing i service discovery

### Asinhrona komunikacija (RabbitMQ)
- Order Service → RabbitMQ → Notification Service
- Event-driven messaging nakon kreiranja/ažuriranja porudžbine
- Exchange: `order.exchange` (TopicExchange)
- Queue: `order.notification.queue`
- Routing Key: `order.notification`
- Message format: JSON (OrderEventMessage)

## Tehnologije

- Java 17
- Spring Boot 3.2.5
- Spring Cloud 2023.0.1
- Spring Security + JWT (JSON Web Tokens)
- PostgreSQL (Database per service pattern)
- RabbitMQ (Message broker)
- Docker & Docker Compose
- Maven
- JUnit 5 & Mockito
- JaCoCo (Code Coverage)
- GitHub Actions (CI/CD)

## Testiranje

### Pokretanje testova

```bash
# Svi testovi za ceo projekat
mvn clean test

# Testovi za specifičan servis
cd product-service
mvn test

# Sa code coverage izveštajem
mvn test jacoco:report

# Pregled coverage izveštaja
open target/site/jacoco/index.html
```

### Test konfiguracija
- **Test baza**: H2 in-memory database
- **Mocking**: Mockito za unit testove
- **Integration testovi**: Spring Boot Test sa MockMvc
- **Feign client testovi**: MockBean za eksterne pozive

## CI/CD Pipeline

Projekat koristi **GitHub Actions** za automatizovani CI/CD proces.

### Pipeline Faze

```
BUILD → TEST → INTEGRATION-TEST → PACKAGE → DEPLOY
```

### Workflow Triggeri
- **Push na `develop`**: Pokreće pipeline i deploy na Development
- **Push na `main`**: Pokreće pipeline i deploy na Staging + Production (manual approval)
- **Pull Request**: Pokreće BUILD + TEST faze

### Deployment Okruženja

| Environment | Branch | Deployment | URL |
|-------------|--------|------------|-----|
| Development | `develop` | Automatski | http://dev.yourapp.com |
| Staging | `main` | Automatski | http://staging.yourapp.com |
| Production | `main` | Manual approval | http://yourapp.com |

### Detaljnije informacije

Za detaljne informacije o pipeline-u, pogledaj [PIPELINE.md](PIPELINE.md).

## Pokretanje Sistema

### Brzo pokretanje (Preporučeno)
```bash
# Build all services
mvn clean package -DskipTests

# Start all services with Docker Compose
docker-compose up -d

# Sačekaj 30-60 sekundi da se servisi pokrenu
# Proveri status
docker-compose ps
```

### Provera da li sistem radi
- **Eureka Dashboard**: http://localhost:8761
- **RabbitMQ Management UI**: http://localhost:15672 (guest/guest)
- **API Gateway**: http://localhost:8080
- **Health Checks**: `http://localhost:808X/actuator/health` (gde X je 1-6 za svaki servis)

## Početni Podaci (Seed Data)

Sistem automatski učitava početne podatke iz `data.sql` fajlova pri prvom pokretanju:

### Proizvodi (15 proizvoda)
### Korisnici (6 korisnika)
#### Admin nalog:
- **Email**: `admin@ecommerce.com`
- **Password**: `admin123`
- **Role**: ADMIN
#### Obični korisnici:
- **Email**: `john.doe@example.com` | **Password**: `password123` | **Role**: USER
- **Email**: `jane.doe@example.com` | **Password**: `password123` | **Role**: USER
- **Email**: `bob.smith@example.com` | **Password**: `password123` | **Role**: USER
- **Email**: `alice.johnson@example.com` | **Password**: `password123` | **Role**: USER

**Napomena**: Svi korisnici koriste BCrypt hash-ovane lozinke.

## Testiranje Funkcionalnosti

### Kako Testirati Sinhronu i Asinhroonu Komunikaciju

Kada kreirate porudžbinu kroz Order Service API, sistem automatski:

1. **Sinhrono**: Order Service → Product Service (OpenFeign)
   - Validira da proizvod postoji
   - Proverava zalihe
   - Preuzima cenu i detalje

2. **Asinhrono**: Order Service → RabbitMQ → Notification Service
   - Order Service šalje poruku u RabbitMQ nakon kreiranja porudžbine
   - Notification Service prima poruku iz queue-a
   - Kreira notifikaciju i čuva u bazi

### Verifikacija
- **Porudžbine**: Proveri u order-db (orders tabela)
- **Notifikacije**: Proveri u notification-db (notifications tabela)
- **RabbitMQ Queue**: http://localhost:15672 → Queues → order.notification.queue

## Napomene

### Arhitekturni Paterni
- **Database per Service**: Svaki mikroservis ima svoju nezavisnu PostgreSQL bazu podataka
- **Service Discovery**: Eureka server za automatsku registraciju i discovery servisa
- **API Gateway**: Centralizovani routing i load balancing
- **Event-Driven Architecture**: Asinhrona komunikacija kroz RabbitMQ message broker

### Data Persistence
- **Docker Volumes**: Svi podaci se čuvaju u Docker volumes i ostaju trajni posle restarta
- **Automatic Seeding**: `data.sql` fajlovi automatski popunjavaju baze pri prvom pokretanju
- **Hibernate DDL**: `ddl-auto: update` automatski kreira/ažurira tabele

### Komunikacija
- **Sinhrona**: Spring Cloud OpenFeign - deklarativni REST klijent sa load balancing-om
- **Asinhrona**: Spring AMQP - event-driven messaging kroz RabbitMQ
