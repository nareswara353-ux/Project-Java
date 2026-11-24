# ADR-0005: 3-Tier Testing Pyramid

- **Status**: Accepted
- **Tanggal**: 2026-02-01
- **Deciders**: Principal Engineer, QA Lead
- **Related ADRs**: ADR-0003, ADR-0006

## Context

Testing strategy harus mendemonstrasikan kemampuan **enterprise engineering**:

1. **Fast feedback** — developer harus dapat feedback < 30 detik saat push
2. **Production parity** — test harus jalan di infrastructure yang mirip production
3. **Confidence** — deployment harus percaya test yang lulus = production-ready

Masalah umum: hanya unit test (tidak verifikasi wiring) atau hanya integration test (lambat, flaky).

## Decision

Mengadopsi **3-tier testing pyramid**:

| Tier | Teknologi | Jumlah | Runtime | Fungsi |
|------|-----------|--------|---------|--------|
| **Unit** | JUnit 5 + Mockito | ~60 | < 5s | Logic verification |
| **Integration (H2)** | `@SpringBootTest` + H2 | ~25 | ~30s | Wiring verification |
| **Integration (PostgreSQL)** | Testcontainers + PG16 | ~11 | ~60s | Production parity |

**Distribution**: 80% unit / 15% integration-H2 / 5% integration-PG — sesuai pyramid.

**Rules**:
- Semua endpoint harus punya **integration test** (H2) — verifikasi auth, validation, HTTP status
- Semua use-case critical (product CRUD, auth flow) harus punya **PostgreSQL test** — verifikasi dialect-specific behavior
- Unit test WAJIB untuk business logic — tidak perlu Spring context

**Container strategy**:
- `@Testcontainers(disabledWithoutDocker = true)` — skip aman jika Docker tidak ada
- `.withReuse(true)` — container dipakai ulang antar test class (hemat ~5 detik/class)
- `@ServiceConnection` — Spring Boot auto-config datasource

## Consequences

### Positif

- **Fast feedback lokal** — `mvn test` (unit + H2) < 60 detik
- **Production parity** — PostgreSQL test verifikasi adapter di DB asli
- **No flaky** — Testcontainers isolated per test class
- **Safe CI** — `disabledWithoutDocker` + `flyway.enabled=false` di test properties

### Negatif

- **CI butuh Docker** — GitHub Actions ubuntu-latest sudah include Docker, tapi build time +60 detik
- **Container startup** — ~10-15 detik per test run (mitigated by `.withReuse(true)` lokal)
- **Storage** — Testcontainers butuh disk space (~1GB per image)

### Netral

- Developer perlu Docker Desktop di lokal
- Coverage gate (JaCoCo 70%) harus disesuaikan jika tier baru ditambah

## Alternatives Considered

| Alternatif | Alasan Ditolak |
|------------|----------------|
| **Only unit tests** | Tidak verifikasi wiring Spring, tidak yakin dengan deployment |
| **Only integration tests** | Lambat (> 3 menit), flaky, feedback loop jelek |
| **Embedded PostgreSQL (embedded-postgres)** | Binary besar, arsitektur-specific, tidak container-native |
| **H2 only** | Dialect mismatch (jsonb, sequence behavior, timezone) — bukan production parity |

## References

- Martin Fowler, "The Practical Test Pyramid"
- Testcontainers Documentation
