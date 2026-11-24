# ADR-0001: Adopsi Clean Architecture dengan Ports & Adapters

- **Status**: Accepted
- **Tanggal**: 2026-01-15
- **Deciders**: Principal Engineer, Backend Team
- **Related ADRs**: ADR-0002, ADR-0003

## Context

Project harus jadi portofolio yang menunjukkan kemampuan **enterprise architecture**. Masalah umum di project Java: business logic tercampur dengan framework (Spring, JPA), sehingga:

1. **Sulit di-test** — logika bisnis butuh Spring context penuh untuk dijalankan
2. **Vendor lock-in** — ganti database/ORM = refactor besar
3. **Business logic tidak jelas** — tersembunyi di antara anotasi framework

Portofolio ini harus mendemonstrasikan bagaimana **memisahkan domain dari infrastruktur** secara clean.

## Decision

Mengadopsi **Clean Architecture** (Robert C. Martin) dengan **Ports & Adapters** (Alistair Cockburn), diorganisir dalam 4 layer:
interfaces → REST controllers, DTOs, exception handlers
application → use-case services (ProductService, AuthService)
domain → entities (Product, User, Role), port interfaces, events
infrastructure → JPA adapters, security, listeners, schedulers

text

**Aturan dependency**: setiap layer hanya boleh depend ke layer yang lebih dalam (domain). Domain **tidak boleh** import Spring, JPA, atau framework lain.

Port (interface) didefinisikan di domain:
- `ProductRepository` (domain) → diimplementasikan `ProductRepositoryAdapter` (infrastructure)
- `EventPublisher` (application) → diimplementasikan `SpringEventPublisherAdapter` (infrastructure)

## Consequences

### Positif

- **Testability tinggi** — domain logic di-test tanpa Spring context (60+ unit test < 5 detik)
- **Framework-agnostic domain** — bisa ganti Spring Boot ke Quarkus/Micronaut tanpa sentuh domain
- **Jelas batas modul** — tim baru bisa on-board cepat dengan melihat struktur package
- **Business rules terlihat eksplisit** — `Product` record punya validasi, tidak tersebar

### Negatif

- **Lebih banyak file** — mapping domain ↔ entity (2x class per model)
- **Boilerplate** — adapter pattern butuh DTO conversion di boundary
- **Overengineering untuk aplikasi kecil** — CRUD sederhana jadi 3 layer

### Netral

- Perlu disiplin tim untuk tidak "curang" import infrastructure di domain
- Perlu konvensi kode review untuk menjaga batas layer

## Alternatives Considered

| Alternatif | Alasan Ditolak |
|------------|----------------|
| Layered Architecture (controller → service → repo) | Business logic tetap tercampur service, domain tidak terisolasi |
| Hexagonal Architecture (full) | Sama seperti Clean Architecture, tapi komunitas lebih kecil dan tooling terbatas |
| Framework-driven (service = domain) | Paling cepat di awal, tapi portfolio tidak menunjukkan skill arsitektur |

## References

- Robert C. Martin, "Clean Architecture" (2017)
- Alistair Cockburn, "Hexagonal Architecture" (2005)
