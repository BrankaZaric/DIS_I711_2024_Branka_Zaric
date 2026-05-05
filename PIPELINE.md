# Pipeline Dokumentacija

## CI/CD Pipeline Pregled

Projekat koristi **GitHub Actions** za automatizovani CI/CD proces koji se sastoji od sledećih faza:

```
┌─────────┐     ┌─────────┐     ┌─────────────────┐     ┌─────────┐     ┌────────────┐
│  BUILD  │ --> │  TEST   │ --> │ INTEGRATION TEST│ --> │ PACKAGE │ --> │   DEPLOY   │
└─────────┘     └─────────┘     └─────────────────┘     └─────────┘     └────────────┘
```

## Faze Pipeline-a

### 1. BUILD Faza
**Cilj:** Kompajliranje svih mikroservisa

```bash
mvn clean compile -DskipTests
```

**Šta se dešava:**
- Checkout koda iz repozitorijuma
- Setup JDK 17
- Kompajliranje svih modula
- Keširање Maven zavisnosti

### 2. TEST Faza
**Cilj:** Pokretanje unit i integracionih testova za svaki servis

```bash
cd <service-name>
mvn test
```

**Paralelno testiranje:**
- Svaki servis se testira u posebnom job-u
- Matrix strategija za istovremeno pokretanje testova
- Generisanje test coverage izveštaja (JaCoCo)

**Servisi koji se testiraju:**
- eureka-server
- config-server
- api-gateway
- product-service (41 test)
- order-service (43 testa)

**Artifakti:**
- Test rezultati (Surefire reports)
- Code coverage izveštaji (JaCoCo reports)

### 3. INTEGRATION TEST Faza
**Cilj:** Testiranje komunikacije između servisa

```bash
mvn verify -Pintegration-tests
```

**Okruženje:**
- PostgreSQL za Product Service (port 5432)
- PostgreSQL za Order Service (port 5433)
- Health checks za baze podataka

### 4. PACKAGE Faza
**Cilj:** Kreiranje Docker image-a za svaki servis

```bash
cd <service-name>
mvn clean package -DskipTests
docker build -t <service-name>:${GIT_SHA} .
docker tag <service-name>:${GIT_SHA} <service-name>:latest
```

**Izlaz:**
- JAR fajlovi za svaki servis
- Docker image-i (tar.gz arhive)
- Artifakti spremni za deployment

### 5. DEPLOY Faze

#### Development (develop branch)
- Automatski deployment na push
- URL: http://dev.yourapp.com
- Bez manuelnog odobrenja

#### Staging (main branch)
- Automatski deployment na push u main
- URL: http://staging.yourapp.com
- Testiranje pre produkcije

#### Production (main branch)
- Zahteva manuelno odobrenje
- URL: http://yourapp.com
- Kreiranje release tag-a

## Pokretanje Pipeline-a

### Automatsko pokretanje:
```bash
# Push na develop - pokreće BUILD, TEST, INTEGRATION TEST, PACKAGE, DEPLOY-DEV
git push origin develop

# Push na main - pokreće sve faze uključujući STAGING i PRODUCTION
git push origin main

# Pull Request - pokreće BUILD, TEST, INTEGRATION TEST
```

### Lokalno testiranje pipeline faza:

#### 1. Build faza:
```bash
mvn clean compile -DskipTests
```

#### 2. Test faza:
```bash
# Svi testovi
mvn test

# Specifičan servis
cd product-service && mvn test

# Sa coverage izveštajem
mvn test jacoco:report
```

#### 3. Integration test faza:
```bash
# Prvo pokreni baze
docker-compose up -d postgres-product postgres-order

# Pokreni integration testove
mvn verify -Pintegration-tests
```

#### 4. Package faza:
```bash
# Build JAR
mvn clean package -DskipTests

# Build Docker image
cd product-service
docker build -t product-service:latest .
```

## Code Coverage

Projekat koristi **JaCoCo** za merenje test coverage-a.

### Generisanje coverage izveštaja:
```bash
cd product-service
mvn clean test jacoco:report
```

### Pregled izveštaja:
```bash
# Otvori u browser-u
open product-service/target/site/jacoco/index.html
```

## Okruženja (Environments)

### Development
- **Branch:** develop
- **Deployment:** Automatski
- **Svrha:** Razvoj i testiranje novih feature-a
- **Baze:** Test podaci

### Staging
- **Branch:** main
- **Deployment:** Automatski nakon uspešnih testova
- **Svrha:** Pre-produkcijsko testiranje
- **Baze:** Produkcijski clone

### Production
- **Branch:** main
- **Deployment:** Manuelno odobrenje obavezno
- **Svrha:** Produkciono okruženje
- **Baze:** Produkcijske baze

## Monitoring Pipeline-a

### GitHub Actions Dashboard:
1. Idi na GitHub repozitorijum
2. Klikni na **Actions** tab
3. Vidi sve pokrenute workflow-e
4. Preuzmi artifakte (test results, coverage reports)

### Status Badge:
Dodaj u README.md:
```markdown
![CI/CD Pipeline](https://github.com/USERNAME/REPO/workflows/CI%2FCD%20Pipeline/badge.svg)
```

## Troubleshooting

### Test failures:
```bash
# Proveri test izveštaje
cat <service>/target/surefire-reports/*.txt

# Pokreni samo failing test
mvn test -Dtest=TestClassName#testMethodName
```

### Build failures:
```bash
# Clean i rebuild
mvn clean install -DskipTests

# Proveri zavisnosti
mvn dependency:tree
```

### Docker build failures:
```bash
# Proveri Dockerfile
cat <service>/Dockerfile

# Build lokalno sa verbose output-om
docker build --progress=plain -t service:test .
```

## Best Practices

1. **Uvek testiraj lokalno pre push-a:**
   ```bash
   mvn clean verify
   ```

2. **Koristi feature branch-eve:**
   ```bash
   git checkout -b feature/new-feature
   git push origin feature/new-feature
   # Kreiraj Pull Request
   ```

3. **Proveri test coverage:**
   ```bash
   mvn test jacoco:report
   # Cilj: minimum 70% coverage
   ```

4. **Merge samo kada su svi testovi zeleni:**
   - Svi checks na PR moraju biti ✅

## Maven Profili

### Default (unit + integration tests):
```bash
mvn test
```

### Samo unit testovi:
```bash
mvn test -DexcludedGroups=integration
```

### Samo integration testovi:
```bash
mvn verify -Pintegration-tests
```

## Artifakti

Pipeline generiše sledeće artifakte:

1. **Test Results** - Surefire reports za svaki servis
2. **Coverage Reports** - JaCoCo HTML izveštaji
3. **Docker Images** - Kompresovani tar.gz fajlovi
4. **JAR Files** - Executable Spring Boot JAR-ovi

### Preuzimanje artifakata:
```bash
# Iz GitHub Actions UI:
Actions → Workflow run → Artifacts sekcija
```