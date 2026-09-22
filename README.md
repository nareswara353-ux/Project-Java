# Java Enterprise Portfolio

![Java CI](https://github.com/nareswara353-ux/Project-Java/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)
![Coverage](https://img.shields.io/badge/Coverage-70%25%2B-yellowgreen)

Proyek portofolio enterprise berbasis **Java 21** dengan **Spring Boot 3.4**, menerapkan **Clean Architecture**, **SOLID principles**, **JWT Authentication + RBAC**, dan **CI/CD pipeline** dengan quality gates (coverage + static analysis).

## Teknologi

- **Java 21** (Records, Sealed Interfaces, Pattern Matching, Switch Expressions)
- **Spring Boot 3.4** (Web, Actuator, Validation, Data JPA, Security)
- **Spring Security 6** + **JWT** (JJWT 0.12)
- **H2 Database** (in-memory) + Hibernate
- **Maven** (dependency management & build)
- **Lombok** (boilerplate reduction)
- **JUnit 5 + Mockito + AssertJ** (testing)
- **JaCoCo** (code coverage, gate 70% line)
- **SpotBugs + FindSecBugs** (static analysis)
- **GitHub Actions** (CI dengan artifact upload)

## Arsitektur

Clean Architecture / Hexagonal Architecture dengan 4 layer:
interfaces → REST controllers, DTOs, validation, global exception handler
application → use-case services (ProductService, AuthService)
domain → business entities (Product, User, Role), port interfaces, events
infrastructure → adapters (JPA repositories, JWT service, security filters, listeners)

text

## Fitur

### Product Management
- CRUD produk lengkap dengan validasi
- Pencarian produk berdasarkan substring nama
- Penyesuaian stok dengan proteksi nilai negatif
- Custom validation (`@UniqueProductName`, `@PriceRange`)
- Domain events + audit log persisten

### Authentication & Authorization
- Registrasi & login dengan BCrypt
- JWT access token + refresh token
- RBAC: `ADMIN` (full CRUD) vs `USER` (read-only)
- Custom 401/403 JSON responses
- Default seeded accounts (`admin/admin123`, `user/user123`)

### Observability & Operations
- Actuator endpoints (health, info, metrics)
- Custom health indicator untuk ProductRepository
- Request logging interceptor
- Swagger UI / OpenAPI docs
- Pagination untuk audit log

### Quality & CI
- JaCoCo line coverage gate **70%**
- SpotBugs + FindSecBugs static analysis
- Artifacts: JAR, coverage report, SpotBugs report
- Automated build & test pada setiap push/PR ke `main`

## Menjalankan Aplikasi

### Prasyarat
- JDK 21+
- Maven 3.9+

### Build & Run
```bash
mvn clean install
mvn spring-boot:run
Aplikasi berjalan di http://localhost:8080/api.
Health check: GET /api/health
Swagger UI: http://localhost:8080/api/swagger-ui.html
H2 Console: http://localhost:8080/api/h2-console

Menjalankan Test + Quality Gates
bash
mvn clean verify
API Endpoints
Authentication (Public)
Method	Endpoint	Deskripsi
POST	/api/auth/register	Registrasi user baru
POST	/api/auth/login	Login, dapat access + refresh token
POST	/api/auth/refresh	Tukar refresh token dengan access token baru
Products (Protected)
Method	Endpoint	Role
GET	/api/products	USER, ADMIN
GET	/api/products/{id}	USER, ADMIN
GET	/api/products/search?name=	USER, ADMIN
POST	/api/products	ADMIN
PUT	/api/products/{id}	ADMIN
PATCH	/api/products/{id}/stock?delta=	ADMIN
DELETE	/api/products/{id}	ADMIN
Audit Logs (Protected)
Method	Endpoint	Role
GET	/api/audit-logs	USER, ADMIN
GET	/api/audit-logs/product/{productId}	USER, ADMIN
Quality Gates
Tool	Ambang	Aksi
JaCoCo	Line coverage ≥ 70%	Build gagal jika di bawah
SpotBugs	Effort Max, Threshold Medium	Build gagal jika temuan baru
FindSecBugs	Security patterns	Build gagal jika temuan baru
Contoh Penggunaan
Login
bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
Akses Protected Endpoint
bash
TOKEN="<access-token-dari-login>"
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"
Buat Produk (ADMIN)
bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,"stock":5}'
Struktur Proyek
text
src/
├── main/
│   ├── java/com/example/enterprise/
│   │   ├── application/
│   │   │   ├── port/         (ProductService, AuthService, AuditLogPort, EventPublisher)
│   │   │   └── service/      (ProductServiceImpl, AuthServiceImpl)
│   │   ├── config/           (Security, Authentication, JWT, OpenAPI, Web, DataInitializer)
│   │   ├── domain/
│   │   │   ├── event/        (ProductEvent sealed hierarchy)
│   │   │   ├── exception/    (ProductNotFoundException, DuplicateProductException)
│   │   │   ├── port/         (ProductRepository, UserRepository)
│   │   │   ├── user/         (User, Role)
│   │   │   └── validation/   (ProductValidator)
│   │   ├── infrastructure/
│   │   │   ├── adapter/      (JPA repos, entity, adapters)
│   │   │   ├── health/       (ProductRepositoryHealthIndicator)
│   │   │   ├── listener/     (ProductEventListener)
│   │   │   └── security/     (JwtService, JwtAuthenticationFilter, CustomUserDetailsService)
│   │   └── interfaces/rest/  (controllers, DTOs, validators, GlobalExceptionHandler)
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/enterprise/
        ├── application/service/
        ├── infrastructure/security/
        └── interfaces/rest/
Lisensi
MIT License. Proyek ini hanya untuk keperluan portofolio.