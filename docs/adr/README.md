# Architecture Decision Records (ADRs)

Kumpulan keputusan arsitektur untuk **Java Enterprise Portfolio**.

## Apa itu ADR?

ADR adalah dokumen singkat yang menangkap **"why"** di balik keputusan teknis penting. Format mengikuti [Michael Nygard's template](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

## Format

Setiap ADR mengikuti struktur:

- **Status**: Proposed / Accepted / Deprecated / Superseded by ADR-XXXX
- **Context**: Situasi/masalah yang memicu keputusan
- **Decision**: Keputusan yang diambil
- **Consequences**: Konsekuensi positif + negatif
- **Alternatives Considered**: Opsi lain yang ditolak beserta alasannya

## Index

| ADR | Judul | Status |
|-----|-------|--------|
| [0001](0001-use-clean-architecture.md) | Adopsi Clean Architecture dengan Ports & Adapters | Accepted |
| [0002](0002-jwt-authentication.md) | Stateless JWT Authentication | Accepted |
| [0003](0003-database-schema-migration.md) | Flyway untuk Schema Migration | Accepted |
| [0004](0004-caffeine-caching.md) | Caffeine untuk In-Memory Caching | Accepted |
| [0005](0005-testing-strategy.md) | 3-Tier Testing Pyramid | Accepted |
| [0006](0006-observability-stack.md) | Full Observability Stack | Accepted |

## Menambahkan ADR Baru

1. Buat file baru dengan format `NNNN-judul-singkat.md`
2. Salin dari `template.md`
3. Isi semua seksi
4. Update index di tabel atas
5. Commit dengan pesan `docs(adr): add ADR-NNNN <judul>`
