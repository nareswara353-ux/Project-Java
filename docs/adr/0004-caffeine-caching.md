# ADR-0004: Caffeine untuk In-Memory Caching

- **Status**: Accepted
- **Tanggal**: 2026-01-28
- **Deciders**: Principal Engineer, Performance Team
- **Related ADRs**: ADR-0001

## Context

Operasi `GET /api/products/{id}` dan `GET /api/products/search?name=X` dipanggil sering (dashboard, mobile app polling) tapi data jarang berubah. Setiap request hit database → overhead connection + query time.

Project butuh **caching layer** yang:

1. **Fast** — sub-millisecond access
2. **Bounded** — tidak OOM jika cache tumbuh
3. **TTL** — data stale otomatis expire
4. **Simple ops** — tidak butuh infrastructure tambahan (Redis cluster)

## Decision

Menggunakan **Caffeine** (Spring Boot starter-cache):

| Cache | TTL | Max Size | Evicted By |
|-------|-----|----------|------------|
| `products` | 5 menit | 1000 entries | Semua write ops |
| `productSearch` | 1 menit | 500 entries | Semua write ops |

**Pattern**:
- `@Cacheable(value = PRODUCTS_CACHE, key = "#id")` di `getProductById`
- `@Caching(evict = { @CacheEvict(PRODUCTS_CACHE, allEntries=true), ... })` di create/update/delete/adjustStock
- Admin endpoint `POST /api/admin/cache/clear` untuk manual invalidation
- `GET /api/admin/cache/stats` untuk observability

## Consequences

### Positif

- **~10x speedup** pada read (dari ~5ms DB ke ~0.5ms cache hit)
- **Bounded memory** — max 1000+500 entries
- **Zero infrastructure** — pure JVM heap, no Redis needed
- **Test-friendly** — pure Java, tidak butuh Testcontainers

### Negatif

- **Distributed cache miss** — kalau scale ke 5 instance, cache tidak shared (bisa stale antar instance)
- **Cold start** — cache kosong saat restart, spike DB di menit pertama
- **Memory pressure** — cache di JVM heap, bisa compete dengan aplikasi

### Netral

- Perlu monitoring cache hit ratio (via Actuator metrics)
- Jika scale > 2 instance, perlu migrate ke Redis (ADR baru)

## Alternatives Considered

| Alternatif | Alasan Ditolak |
|------------|----------------|
| **Redis** | Butuh infrastructure tambahan, overkill untuk single-instance portfolio |
| **Ehcache** | Lebih kompleks untuk use case sederhana, dokumentasi lebih tua |
| **No cache** | Lambat, tidak menunjukkan skill performance engineering |
| **Spring Cache + ConcurrentHashMap** | Tidak ada TTL/eviction policy, bisa OOM |

## References

- Caffeine Documentation
- Spring Boot Cache Abstraction
