# ADR-0003: Flyway untuk Database Schema Migration

- **Status**: Accepted
- **Tanggal**: 2026-01-25
- **Deciders**: Principal Engineer, DBA
- **Related ADRs**: ADR-0001

## Context

Sebelumnya project menggunakan `spring.jpa.hibernate.ddl-auto=update` untuk schema management. Ini adalah **antipattern production** karena:

1. **Tidak ada version tracking** — tidak tahu schema versi apa yang jalan
2. **Tidak reproducible** — schema di dev vs prod bisa drift
3. **Tidak aman untuk rollback** — Hibernate hanya add, tidak drop
4. **Tidak auditable** — tidak ada changelog

Enterprise project butuh **versioned schema** dengan audit trail.

## Decision

Menggunakan **Flyway** untuk database migration:

- `ddl-auto=none` — Flyway adalah sole source of schema truth
- Migrations di `src/main/resources/db/migration/`
- Format: `V<version>__<description>.sql`
- **Tests tetap** `ddl-auto=create-drop` + `flyway.enabled=false` untuk startup cepat

Migration yang ada:
- `V1__initial_schema.sql` — 4 tabel utama + indexes dasar
- `V2__add_performance_indexes.sql` — 5 index tambahan untuk caching & pagination

## Consequences

### Positif

- **Reproducible** — schema dev = staging = prod
- **Auditable** — setiap perubahan punya file + timestamp + versi
- **Rollback-ready** — bisa apply undo migration (manual)
- **CI-verifiable** — `FlywayMigrationIntegrationTest` memverifikasi semua migration di PostgreSQL
- **Baseline existing** — `baseline-on-migrate=true` untuk migration DB yang sudah ada

### Negatif

- **Manual SQL** — tidak auto-generate dari entity, must write DDL manual
- **Type mapping** — harus cocok dengan Hibernate type (H2 vs PostgreSQL berbeda)
- **Learning curve** — tim harus paham Flyway convention

### Netral

- Perlu disiplin tidak edit migration yang sudah applied
- Perlu review migration di PR

## Alternatives Considered

| Alternatif | Alasan Ditolak |
|------------|----------------|
| **Liquibase** | Lebih fleksibel (XML/YAML/JSON/SQL), tapi lebih kompleks untuk project SQL-native |
| **Hibernate ddl-auto=update** | Antipattern production (alasan di Context) |
| **Manual SQL scripts** | Tidak versioned, tidak auditable |

## References

- Flyway Documentation
- Spring Boot + Flyway Integration
