# Portfolio Enterprise - Java 21 + Spring Boot 3.4

Proyek portofolio enterprise yang mendemonstrasikan implementasi **Clean Architecture** (Hexagonal Architecture), **SOLID principles**, dan **design patterns** modern. Dibangun dengan Java 21 dan Spring Boot 3.4, dilengkapi dengan CI/CD pipeline menggunakan GitHub Actions.

---

## Daftar Isi

- [Teknologi](#-teknologi)
- [Arsitektur](#-arsitektur)
- [Fitur Utama](#-fitur-utama)
- [Struktur Proyek](#-struktur-proyek)
- [Setup & Menjalankan Aplikasi](#-setup--menjalankan-aplikasi)
- [API Endpoints](#-api-endpoints)
- [Testing](#-testing)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Validasi & Error Handling](#-validasi--error-handling)
- [Best Practices](#-best-practices)
- [Catatan Developer](#-catatan-developer)

---

## Teknologi

| Komponen | Versi | Kegunaan |
|----------|-------|----------|
| **Java** | 21 | Language (Records, Pattern Matching, Sealed Classes) |
| **Spring Boot** | 3.4 | Framework (Web, Actuator, Validation) |
| **Maven** | 3.9+ | Build & Dependency Management |
| **Lombok** | Latest | Boilerplate Reduction (@Data, @Slf4j, @RequiredArgsConstructor) |
| **JUnit 5** | Latest | Unit Testing Framework |
| **Mockito** | Latest | Object Mocking |
| **Spring Test** | 3.4 | Integration Testing |
| **GitHub Actions** | Built-in | CI/CD Pipeline |

### Java 21 Features yang Digunakan

- **Records**: Immutable data carriers untuk DTOs dan domain entities
- **Pattern Matching**: Switch expressions dengan type patterns
- **Sealed Classes**: Restricted inheritance untuk type hierarchy
- **Virtual Threads** (Project Loom): Lightweight concurrency
- **Text Blocks**: Multi-line strings untuk SQL queries

---

## Arsitektur

Proyek mengikuti **Clean Architecture** dengan **Hexagonal Architecture** pattern - memisahkan business logic dari framework dependencies.

```
┌─────────────────────────────────────────────────────────┐
│                  Interfaces Layer                        │
│  REST Controllers, DTOs, Validation, Global Exception   │
│              Handling, API Documentation                │
├─────────────────────────────────────────────────────────┤
│               Application Layer                          │
│  Use-Case Services (ProductService), Orchestration      │
│              Dependency on Ports (interfaces)            │
├─────────────────────────────────────────────────────────┤
│                  Domain Layer                            │
│  Business Entities (Product), Port Interfaces            │
│          (ProductRepository), Business Rules            │
│           Pure Java - No Framework Dependencies         │
├─────────────────────────────────────────────────────────┤
│              Infrastructure Layer                        │
│  Adapter Implementations (InMemoryProductRepository)    │
│    Database Connections, External Service Integration   │
└─────────────────────────────────────────────────────────┘
```

### Layer Details

#### **Interfaces Layer** (Presentation/API)
```
├── rest/
│   ├── ProductController.java          # REST endpoints
│   ├── HealthController.java           # Health check
│   ├── dto/
│   │   ├── ProductRequest.java         # Input DTO (Record)
│   │   ├── ProductResponse.java        # Output DTO (Record)
│   │   └── ErrorResponse.java
│   ├── validation/
│   │   └── UniqueProductNameValidator.java  # Custom validator
│   └── exception/
│       └── GlobalExceptionHandler.java # Centralized error handling
```

#### **Application Layer** (Business Logic Orchestration)
```
├── service/
│   └── ProductService.java             # Use-case implementation
└── port/
    └── ProductRepository.java          # Port interface (abstraction)
```

#### **Domain Layer** (Core Business)
```
├── entity/
│   └── Product.java                    # Business entity (Record/Sealed)
├── exception/
│   ├── ProductNotFound.java
│   ├── DuplicateProductName.java
│   └── InvalidProductData.java
└── port/
    └── ProductRepository.java          # Port interface definition
```

#### **Infrastructure Layer** (Persistence)
```
├── adapter/
│   ├── InMemoryProductRepository.java  # In-memory implementation
│   └── JpaProductRepository.java       # (Jika menggunakan database)
```

---

## Fitur Utama

### 1. **CRUD Operasi Produk**
- ✅ **Create**: Membuat produk baru dengan validasi
- ✅ **Read**: Ambil produk berdasarkan ID atau semua produk
- ✅ **Update**: Update data produk lengkap
- ✅ **Delete**: Hapus produk dari sistem

### 2. **Pencarian Produk**
- Cari berdasarkan substring nama (case-insensitive)
- Endpoint: `GET /api/products/search?name={substring}`

### 3. **Penyesuaian Stok**
- Tambah atau kurangi stok produk dengan PATCH
- Validasi tidak boleh stok negatif
- Endpoint: `PATCH /api/products/{id}/stock?delta={n}`

### 4. **Validasi Input Komprehensif**
- `@NotBlank`: String tidak boleh kosong
- `@Positive`: Harga harus positif
- `@PositiveOrZero`: Stok tidak boleh negatif
- `@UniqueProductName`: Nama produk harus unik (custom validator)

### 5. **Error Handling Terpusat**
- Global exception handler dengan `@RestControllerAdvice`
- Konsisten JSON response untuk semua error
- HTTP status codes yang appropriate
- Detailed error messages untuk debugging

### 6. **Health Check Endpoint**
- `GET /api/health` - Status aplikasi
- Menggunakan Spring Actuator

---

## Struktur Proyek

```
portfolio-enterprise/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/enterprise/
│   │   │   ├── interfaces/
│   │   │   │   ├── rest/
│   │   │   │   │   ├── ProductController.java
│   │   │   │   │   ├── HealthController.java
│   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── ProductRequest.java    (Record)
│   │   │   │   │   │   ├── ProductResponse.java   (Record)
│   │   │   │   │   │   ├── ErrorResponse.java     (Record)
│   │   │   │   │   │   └── HealthResponse.java    (Record)
│   │   │   │   │   ├── validation/
│   │   │   │   │   │   └── UniqueProductNameValidator.java
│   │   │   │   │   └── exception/
│   │   │   │   │       └── GlobalExceptionHandler.java
│   │   │   │   └── config/
│   │   │   │       └── SwaggerConfig.java
│   │   │   │
│   │   │   ├── application/
│   │   │   │   ├── service/
│   │   │   │   │   └── ProductService.java        # Use-case
│   │   │   │   └── port/
│   │   │   │       └── ProductRepository.java     # Port interface
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   └── Product.java               (Record/Sealed)
│   │   │   │   ├── exception/
│   │   │   │   │   ├── ProductNotFoundException.java
│   │   │   │   │   ├── DuplicateProductNameException.java
│   │   │   │   │   └── InvalidProductDataException.java
│   │   │   │   └── port/
│   │   │   │       └── ProductRepository.java     # Port definition
│   │   │   │
│   │   │   ├── infrastructure/
│   │   │   │   └── adapter/
│   │   │   │       └── InMemoryProductRepository.java  # Adapter
│   │   │   │
│   │   │   ├── EnterpriseApplication.java         # Main entry point
│   │   │   └── ApplicationConfig.java             # Spring configuration
│   │   │
│   │   └── resources/
│   │       ├── application.properties              # Configuration
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   │
│   └── test/
│       └── java/com/example/enterprise/
│           ├── application/
│           │   └── service/
│           │       └── ProductServiceTest.java    # Unit tests
│           │
│           ├── interfaces/
│           │   └── rest/
│           │       └── ProductControllerTest.java # Integration tests
│           │
│           └── infrastructure/
│               └── adapter/
│                   └── InMemoryProductRepositoryTest.java
│
├── .github/
│   └── workflows/
│       └── ci.yml                                  # GitHub Actions CI
│
├── pom.xml                                         # Maven configuration
├── README.md                                       # This file
└── .gitignore
```

---

## Setup & Menjalankan Aplikasi

### Prasyarat

- **JDK 21+** - [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.9+** - [Download Maven](https://maven.apache.org/download.cgi)
- **Git** (opsional) - [Download Git](https://git-scm.com/)

### Langkah-langkah Instalasi

#### 1. Clone Repository

```bash
git clone https://github.com/yourusername/portfolio-enterprise.git
cd portfolio-enterprise
```

#### 2. Verifikasi Java & Maven Version

```bash
java -version
# Output: openjdk version "21.0.x" ...

mvn --version
# Output: Apache Maven 3.9.x ...
```

#### 3. Build Proyek

```bash
mvn clean install
```

Perintah ini akan:
- Download dependencies
- Compile source code
- Run unit tests
- Package aplikasi

#### 4. Jalankan Aplikasi

**Menggunakan Spring Boot Maven Plugin:**

```bash
mvn spring-boot:run
```

**Atau menggunakan JAR file:**

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/portfolio-enterprise-1.0.0.jar
```

#### 5. Verifikasi Aplikasi Berjalan

```bash
# Terminal 1 - Jalankan aplikasi (di atas)

# Terminal 2 - Test health endpoint
curl http://localhost:8080/api/health

# Response:
# {"status":"UP","timestamp":"2024-01-15T10:30:45Z"}
```

Aplikasi akan berjalan di **http://localhost:8080**

---

## API Endpoints

### Endpoints Overview

| Method | Endpoint | Deskripsi | Auth |
|--------|----------|-----------|------|
| **POST** | `/api/products` | Buat produk baru | ❌ |
| **GET** | `/api/products` | Daftar semua produk | ❌ |
| **GET** | `/api/products/{id}` | Ambil produk by ID | ❌ |
| **PUT** | `/api/products/{id}` | Update produk | ❌ |
| **DELETE** | `/api/products/{id}` | Hapus produk | ❌ |
| **GET** | `/api/products/search` | Cari produk by nama | ❌ |
| **PATCH** | `/api/products/{id}/stock` | Adjust stok | ❌ |
| **GET** | `/api/health` | Health check | ❌ |

---

### Detailed Endpoints

#### POST /api/products - Create Product

**Request:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell XPS 13",
    "description": "Ultra-portable laptop with 13-inch display",
    "price": 999.99,
    "stock": 50
  }'
```

**Request Body (ProductRequest Record):**
```json
{
  "name": "Laptop Dell XPS 13",
  "description": "Ultra-portable laptop with 13-inch display",
  "price": 999.99,
  "stock": 50
}
```

**Validation Rules:**
- `name` - Required (@NotBlank), unique (case-insensitive), max 255 chars
- `description` - Optional, max 1000 chars
- `price` - Required (@Positive), must be > 0
- `stock` - Required (@PositiveOrZero), must be >= 0

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Laptop Dell XPS 13",
  "description": "Ultra-portable laptop with 13-inch display",
  "price": 999.99,
  "stock": 50,
  "createdAt": "2024-01-15T10:30:45Z"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2024-01-15T10:30:45Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Product name must be unique",
  "path": "/api/products"
}
```

---

#### GET /api/products - Get All Products

**Request:**
```bash
curl http://localhost:8080/api/products
```

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Laptop Dell XPS 13",
    "description": "Ultra-portable laptop",
    "price": 999.99,
    "stock": 50,
    "createdAt": "2024-01-15T10:30:45Z"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Monitor Samsung 4K",
    "description": "4K Ultra HD Monitor",
    "price": 299.99,
    "stock": 30,
    "createdAt": "2024-01-15T10:31:00Z"
  }
]
```

---

#### GET /api/products/{id} - Get Product by ID

**Request:**
```bash
curl http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Laptop Dell XPS 13",
  "description": "Ultra-portable laptop",
  "price": 999.99,
  "stock": 50,
  "createdAt": "2024-01-15T10:30:45Z"
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2024-01-15T10:30:45Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product with ID 550e8400-e29b-41d4-a716-446655440000 not found",
  "path": "/api/products/550e8400-e29b-41d4-a716-446655440000"
}
```

---

#### PUT /api/products/{id} - Update Product

**Request:**
```bash
curl -X PUT http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell XPS 13 Plus",
    "description": "Updated ultra-portable laptop",
    "price": 1099.99,
    "stock": 45
  }'
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Laptop Dell XPS 13 Plus",
  "description": "Updated ultra-portable laptop",
  "price": 1099.99,
  "stock": 45,
  "createdAt": "2024-01-15T10:30:45Z"
}
```

---

#### DELETE /api/products/{id} - Delete Product

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000
```

**Response (204 No Content):**
```
(empty body)
```

---

#### GET /api/products/search - Search Products

**Request (Case-Insensitive):**
```bash
curl "http://localhost:8080/api/products/search?name=laptop"
```

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Laptop Dell XPS 13",
    "description": "Ultra-portable laptop",
    "price": 999.99,
    "stock": 50,
    "createdAt": "2024-01-15T10:30:45Z"
  }
]
```

---

#### PATCH /api/products/{id}/stock - Adjust Stock

**Request (Increase Stock by 10):**
```bash
curl -X PATCH "http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000/stock?delta=10"
```

**Request (Decrease Stock by 5):**
```bash
curl -X PATCH "http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000/stock?delta=-5"
```

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Laptop Dell XPS 13",
  "description": "Ultra-portable laptop",
  "price": 999.99,
  "stock": 55,
  "createdAt": "2024-01-15T10:30:45Z"
}
```

**Error Response (400 Bad Request - Negative Stock):**
```json
{
  "timestamp": "2024-01-15T10:30:45Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Stock cannot be negative",
  "path": "/api/products/550e8400-e29b-41d4-a716-446655440000/stock"
}
```

---

#### GET /api/health - Health Check

**Request:**
```bash
curl http://localhost:8080/api/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:45Z",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

---

## Testing

### Menjalankan Tests

```bash
# Jalankan semua tests
mvn test

# Jalankan test dengan coverage report
mvn test jacoco:report

# Jalankan test spesifik class
mvn test -Dtest=ProductServiceTest

# Jalankan test dengan pattern
mvn test -Dtest=*ServiceTest
```

### Test Structure

#### Unit Tests (ProductServiceTest)

```java
@DisplayName("ProductService Tests")
class ProductServiceTest {

    private ProductService productService;
    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        productService = new ProductService(repository);
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Arrange
        ProductRequest request = new ProductRequest(
            "Laptop",
            "High-performance laptop",
            999.99,
            50
        );

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertThat(response)
            .isNotNull()
            .extracting("name", "price", "stock")
            .containsExactly("Laptop", 999.99, 50);
    }

    @Test
    @DisplayName("Should throw exception for duplicate name")
    void testCreateProduct_DuplicateName() {
        // Arrange
        ProductRequest request = new ProductRequest("Laptop", "...", 999.99, 50);
        when(repository.findByName("Laptop"))
            .thenReturn(Optional.of(new Product(...)));

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(request))
            .isInstanceOf(DuplicateProductNameException.class);
    }
}
```

#### Integration Tests (ProductControllerTest)

```java
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductController Integration Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should create product via POST endpoint")
    void testCreateProduct_Integration() throws Exception {
        String requestBody = """
            {
              "name": "Laptop",
              "description": "High-performance laptop",
              "price": 999.99,
              "stock": 50
            }
            """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    @DisplayName("Should return 400 for invalid product")
    void testCreateProduct_InvalidData() throws Exception {
        String requestBody = """
            {
              "name": "",
              "price": -100,
              "stock": -10
            }
            """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }
}
```

### Test Coverage Target

| Layer | Target Coverage |
|-------|-----------------|
| Domain Entities | 95%+ |
| Services | 90%+ |
| Controllers | 85%+ |
| Adapters | 80%+ |
| **Overall** | **85%+** |

### Coverage Report

```bash
# Generate coverage report
mvn test jacoco:report

# View report at: target/site/jacoco/index.html
```

---

## CI/CD Pipeline

### GitHub Actions Workflow

File `.github/workflows/ci.yml`:

```yaml
name: CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Setup JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      - name: Build and Test
        run: mvn clean verify

      - name: Generate Coverage Report
        run: mvn jacoco:report

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml

      - name: Build JAR artifact
        run: mvn clean package -DskipTests

      - name: Upload JAR artifact
        uses: actions/upload-artifact@v3
        with:
          name: portfolio-enterprise-${{ github.sha }}
          path: target/*.jar
```

### Pipeline Workflow

Pipeline dijalankan secara otomatis pada:
- Push ke branch `main` atau `develop`
- Pull request ke branch `main` atau `develop`

**Tahapan Pipeline:**
1. **Checkout** - Clone kode dari repository
2. **Setup JDK 21** - Install Java 21 (Temurin)
3. **Cache Dependencies** - Cache Maven packages untuk mempercepat build
4. **Build & Test** - `mvn clean verify` (compile, test, package)
5. **Coverage Report** - Generate JaCoCo coverage report
6. **Upload Coverage** - Push report ke Codecov
7. **Build Artifact** - Package JAR file
8. **Upload Artifact** - Simpan JAR sebagai artifact

### Viewing Pipeline Results

1. **GitHub Actions Tab** - https://github.com/yourusername/portfolio-enterprise/actions
2. **Artifact Download** - Artifacts tersedia di workflow run summary
3. **Coverage Dashboard** - https://codecov.io/gh/yourusername/portfolio-enterprise

---

## ✔️ Validasi & Error Handling

### Input Validation

#### Built-in Validators

```java
public record ProductRequest(
    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    String name,
    
    @Size(max = 1000)
    String description,
    
    @Positive(message = "Price must be greater than 0")
    BigDecimal price,
    
    @PositiveOrZero(message = "Stock cannot be negative")
    Integer stock
) {}
```

#### Custom Validator (@UniqueProductName)

```java
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueProductNameValidator.class)
public @interface UniqueProductName {
    String message() default "Product name must be unique";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class UniqueProductNameValidator 
    implements ConstraintValidator<UniqueProductName, String> {
    
    @Autowired
    private ProductRepository repository;

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return name == null || !repository.existsByNameIgnoreCase(name);
    }
}

// Usage
public record ProductRequest(
    @UniqueProductName
    String name,
    // ...
) {}
```

### Global Exception Handling

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex) {
        log.warn("Product not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND)
            .body(new ErrorResponse(
                NOW,
                NOT_FOUND.value(),
                "Product Not Found",
                ex.getMessage()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(joining(", "));

        return ResponseEntity.status(BAD_REQUEST)
            .body(new ErrorResponse(
                NOW,
                BAD_REQUEST.value(),
                "Validation Failed",
                message
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                NOW,
                INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred"
            ));
    }
}
```

### Error Response Format

```java
public record ErrorResponse(
    Instant timestamp,
    Integer status,
    String error,
    String message
) {}
```

**Example Error Response (400):**
```json
{
  "timestamp": "2024-01-15T10:30:45Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Product name is required, Price must be greater than 0"
}
```

---

## Best Practices

### 1. **Clean Architecture**
- Separation of concerns (layers)
- Domain-driven design
- No framework dependencies in domain layer
- Dependency inversion (interfaces for abstraction)

### 2. **SOLID Principles**

| Prinsip | Implementasi |
|---------|--------------|
| **S**ingle Responsibility | Setiap class punya satu tanggung jawab |
| **O**pen/Closed | Open for extension, closed for modification |
| **L**iskov Substitution | Subtypes dapat mengganti base types |
| **I**nterface Segregation | Interfaces spesifik, bukan generic |
| **D**ependency Inversion | Depend on abstractions, not concretions |

### 3. **Design Patterns**

| Pattern | Penggunaan |
|---------|-----------|
| **Repository** | `ProductRepository` interface & implementation |
| **Service** | `ProductService` untuk use-cases |
| **DTO** | `ProductRequest`, `ProductResponse` (Records) |
| **Adapter** | `InMemoryProductRepository` adapter |
| **Factory** | Spring beans factory |
| **Decorator** | Spring AOP & validators |

### 4. **Immutability**
```java
// Menggunakan Java Records untuk immutable data
public record ProductRequest(
    String name,
    String description,
    BigDecimal price,
    Integer stock
) {}

public record ProductResponse(
    String id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    Instant createdAt
) {}
```

### 5. **Type Safety**
```java
// Sealed classes untuk type hierarchy kontrol
public sealed interface DomainException 
    permits ProductNotFoundException, 
            DuplicateProductNameException,
            InvalidProductDataException {
    // ...
}
```

### 6. **Testability**
- Dependency injection untuk mock dependencies
- Separation of concerns untuk isolated testing
- No static methods atau singletons
- Constructor injection (tidak field injection)

### 7. **Logging Best Practices**
```java
@Slf4j
public class ProductService {
    
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.name());
        try {
            // business logic
            log.info("Product created successfully with ID: {}", productId);
            return response;
        } catch (Exception e) {
            log.error("Failed to create product: {}", request.name(), e);
            throw e;
        }
    }
}
```

---

## Catatan Developer

### Membuat Fitur Baru

Ikuti pattern berikut untuk menambah fitur baru:

1. **Domain Layer** - Define entity dan port interface
   ```java
   public sealed record Feature(...) permits FeatureImpl {}
   
   public interface FeatureRepository {
       Feature save(Feature feature);
       // ...
   }
   ```

2. **Application Layer** - Create service dengan dependency injection
   ```java
   @Service
   @RequiredArgsConstructor
   public class FeatureService {
       private final FeatureRepository repository;
       
       public FeatureResponse create(FeatureRequest request) {
           // use-case logic
       }
   }
   ```

3. **Infrastructure Layer** - Implement repository adapter
   ```java
   @Repository
   public class InMemoryFeatureRepository implements FeatureRepository {
       // implementation
   }
   ```

4. **Interfaces Layer** - Create REST controller
   ```java
   @RestController
   @RequestMapping("/api/features")
   @RequiredArgsConstructor
   public class FeatureController {
       private final FeatureService service;
       
       @PostMapping
       public ResponseEntity<FeatureResponse> create(@RequestBody FeatureRequest request) {
           return ResponseEntity.status(CREATED)
               .body(service.create(request));
       }
   }
   ```

5. **Tests** - Write unit & integration tests
   ```java
   @DisplayName("FeatureService Tests")
   class FeatureServiceTest {
       // unit tests
   }
   
   @SpringBootTest
   class FeatureControllerTest {
       // integration tests
   }
   ```

### Java 21 Features

**Records** - Immutable data carriers:
```java
public record Product(
    String id,
    String name,
    BigDecimal price,
    Integer stock
) {}
```

**Pattern Matching** - Switch expressions:
```java
String status = switch(product) {
    case Product p when p.stock() == 0 -> "Out of Stock";
    case Product p when p.stock() < 10 -> "Low Stock";
    case Product p -> "In Stock";
};
```

**Virtual Threads** - Lightweight concurrency (Project Loom):
```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> {
    // lightweight task
});
```

### Konvensi Coding

- **Naming**: PascalCase untuk classes, camelCase untuk methods/variables
- **Javadoc**: Document public APIs
- **Error Messages**: Clear, actionable error messages
- **Comments**: Explain "why", not "what"
- **Constants**: Use UPPER_SNAKE_CASE

### Database Migration (Future Enhancement)

Untuk integrate dengan database real:

```xml
<!-- Add dependency di pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```java
// Implement JPA repository
@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {
    Optional<ProductEntity> findByNameIgnoreCase(String name);
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
}
```

---

## Pom.xml Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Code Coverage -->
    <dependency>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.10</version>
    </dependency>
</dependencies>
```

---

## Referensi & Resources

- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Java 21 Release Notes](https://www.oracle.com/java/technologies/javase/21all-relnotes.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

---

## Lisensi

Proyek ini dibuat untuk keperluan **portfolio** mendemonstrasikan:

 Clean Architecture & Hexagonal Architecture  
 SOLID Principles Implementation  
 Design Patterns (Repository, Service, DTO, Adapter)  
 Spring Boot 3.4 Best Practices  
 Java 21 Modern Features  
 Unit & Integration Testing  
 CI/CD Pipeline dengan GitHub Actions  
 Input Validation & Error Handling  
 RESTful API Design  

---

## Author

Dibangun oleh **Senior Java Developer & Spring Boot Architect** sebagai portofolio enterprise-grade.

---

## Support & Kontribusi

Untuk pertanyaan atau issues:
1. Buat issue di GitHub repository
2. Kirim email ke developer
3. Hubungi melalui LinkedIn

**Repository**: https://github.com/nareswara353-ux/Project-Java