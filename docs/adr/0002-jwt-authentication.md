# ADR-0002: Stateless JWT Authentication

- **Status**: Accepted
- **Tanggal**: 2026-01-20
- **Deciders**: Principal Engineer, Security Team
- **Related ADRs**: ADR-0001

## Context

Portofolio harus punya **authentication & authorization** yang production-grade, dengan requirement:

1. **REST API** stateless (tidak ada session)
2. **RBAC**: `ADMIN` (full CRUD) vs `USER` (read-only)
3. **Multiple device support** — client mobile + web untuk user yang sama
4. **Scalable** — horizontal scale tanpa sticky session

Pilihan populer: session-based, JWT stateless, atau OAuth2 + OIDC.

## Decision

Menggunakan **stateless JWT** dengan dua token:

| Token | Expiry | Fungsi | Storage (client) |
|-------|--------|--------|-----------------|
| **Access Token** | 1 jam | Otentikasi setiap request | Memory / sessionStorage |
| **Refresh Token** | 24 jam | Tukar access token baru | HttpOnly cookie |

**Library**: JJWT 0.12 (signing HS256).

**Alur**:
1. `POST /api/auth/login` → verify credentials (BCrypt) → return `{accessToken, refreshToken}`
2. Setiap request ke protected endpoint: header `Authorization: Bearer <accessToken>`
3. `JwtAuthenticationFilter` parse token → set `SecurityContext`
4. `@PreAuthorize` / `SecurityFilterChain` enforce RBAC
5. Access token expired → `POST /api/auth/refresh?refreshToken=xxx` → dapat access token baru

**Additional security**:
- BCrypt password hashing (cost 10)
- Rate limiting 5 req/min untuk endpoint login/register (Bucket4j)
- Custom 401/403 handler dengan JSON body

## Consequences

### Positif

- **Stateless** — horizontal scale tanpa sticky session
- **Multi-device** — setiap device punya token sendiri
- **Standard** — compatible dengan mobile app, SPA, CLI
- **Fine-grained expiry** — access token 1 jam, refresh 24 jam → mitigasi token leak

### Negatif

- **Revocation sulit** — JWT tidak bisa di-"invalidate" server-side tanpa blacklist
- **Payload size** — JWT base64 lebih besar dari session id
- **Refresh token harus diamankan** — jika access token terlanjur di local storage (XSS), attacker bisa pakai sampai expiry

### Netral

- Perlu maintenance secret rotation
- Client harus handle `401` → trigger refresh flow

## Alternatives Considered

| Alternatif | Alasan Ditolak |
|------------|----------------|
| **Session-based** (Spring Session + Redis) | Tidak stateless, butuh Redis, sticky session untuk scale |
| **OAuth2 Authorization Server** (full) | Overkill — butuh Auth Server terpisah (Keycloak, UAA) |
| **API Keys** | Statis, tidak ada expiry, tidak ada rotation |

## References

- RFC 7519 — JSON Web Token
- OWASP JWT Cheat Sheet
