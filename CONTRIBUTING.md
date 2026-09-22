# Contributing Guide

Terima kasih atas minat Anda untuk berkontribusi pada **Java Enterprise Portfolio**. Dokumen ini menjelaskan standar dan workflow yang wajib diikuti.

## Prasyarat

- **JDK 21** (disarankan Eclipse Temurin)
- **Maven 3.9+**
- **Git** dengan konfigurasi user.name & user.email
- IDE: IntelliJ IDEA / VS Code dengan extension Java + Lombok

## Setup Development

```bash
git clone https://github.com/nareswara353-ux/Project-Java.git
cd Project-Java
mvn clean install
mvn spring-boot:run
Verifikasi:

Health: GET http://localhost:8080/api/health

Swagger: http://localhost:8080/api/swagger-ui.html

Branching Strategy
Branch	Tujuan
main	Production-ready, protected, hanya via PR
feature/<name>	Fitur baru (contoh: feature/order-service)
fix/<name>	Bugfix (contoh: fix/jwt-expiry-bug)
chore/<name>	Maintenance (contoh: chore/upgrade-spring)
docs/<name>	Dokumentasi
Branch main tidak boleh di-push langsung. Semua perubahan via Pull Request.

Conventional Commits
Format commit wajib:

text
<type>(<scope>): <subject>

[optional body]

[optional footer]
Type yang Diizinkan
Type	Kapan Dipakai
feat	Fitur baru
fix	Bugfix
refactor	Refactor tanpa ubah behavior
test	Menambah/memperbaiki test
docs	Dokumentasi
chore	Maintenance, dependency, config
ci	Perubahan CI/CD
perf	Peningkatan performa
style	Formatting tanpa ubah logika
Contoh Commit
text
feat(product): add pagination to product listing
fix(auth): validate refresh token expiry before issuing access token
test(jwt): cover tampered and expired token scenarios
ci(quality): enforce jacoco line coverage minimum 70%
docs(readme): add badges and quality gates section
Scope Umum
product, auth, jwt, security, audit, config, ci, build, deps, readme

Workflow Pull Request
Fork atau buat branch dari main

Implementasi perubahan

Jalankan test + quality gate lokal:

bash
mvn clean verify
Push branch & buka Pull Request

Pastikan semua checklist berikut terpenuhi

Checklist PR
□ Branch dari main terbaru
□ Mengikuti Conventional Commits
□ mvn clean verify lulus lokal
□ JaCoCo line coverage ≥ 70%
□ Tidak ada temuan SpotBugs severity Medium+
□ Unit test ditambahkan untuk perubahan logika
□ Integration test diperbarui jika endpoint berubah
□ README diperbarui jika API/konfigurasi berubah
□ Tidak ada kredensial/secret yang ter-commit
□ Kode lolos review minimal 1 approver
Quality Gates
CI akan gagal jika salah satu tidak terpenuhi:

Gate	Ambang
Maven build	Wajib sukses
Unit + integration test	100% pass
JaCoCo line coverage	≥ 70%
SpotBugs	Tidak ada temuan Medium+
FindSecBugs	Tidak ada security pattern
Standar Kode
Java
Java 21 features dianjurkan (records, sealed interfaces, pattern matching)

Field final bila memungkinkan

Hindari null return untuk collection → gunakan Optional atau empty collection

Exception domain spesifik (ProductNotFoundException, DuplicateProductException), bukan generic

Tidak ada System.out.println → gunakan SLF4J

Naming
Class: PascalCase

Method/variable: camelCase

Constant: UPPER_SNAKE_CASE

Package: lowercase

Arsitektur
Domain tidak boleh depend ke framework (Spring/JPA)

Application hanya depend ke domain port

Infrastructure implement port

Interfaces (REST) boleh depend application port

Jangan langgar dependency rule — ini inti Clean Architecture.

Testing
Unit test: @ExtendWith(MockitoExtension.class), murni Mockito

Integration test: @SpringBootTest(webEnvironment = RANDOM_PORT) + TestRestTemplate

Controller test: @WebMvcTest + @AutoConfigureMockMvc(addFilters = false)

Assertion: AssertJ (assertThat(...)) lebih disarankan dari JUnit assertion

Keamanan
Jangan commit secret di application.properties → gunakan env var

Password: BCrypt (via PasswordEncoder), jangan pernah plain text

JWT secret: minimal 256-bit, disimpan sebagai environment variable di production

Endpoint publik didaftarkan eksplisit di SecurityConfig

Melaporkan Bug
Buka GitHub Issue dengan template:

text
**Deskripsi**: <apa yang terjadi>
**Ekspektasi**: <apa yang seharusnya>
**Reproduksi**: <langkah detail>
**Environment**: <OS, JDK, Maven version>
**Log**: <error stack trace>
Pertanyaan
Untuk pertanyaan arsitektur atau diskusi desain, buka Discussion atau hubungi maintainer.