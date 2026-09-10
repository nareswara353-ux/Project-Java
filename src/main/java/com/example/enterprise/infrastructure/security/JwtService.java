package com.example.enterprise.infrastructure.security;

import com.example.enterprise.config.JwtProperties;
import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(user.username())
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .claim(CLAIM_ROLES, user.roles().stream().map(Role::name).collect(Collectors.toList()))
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .signWith(signingKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshExpirationMs());

        return Jwts.builder()
                .subject(user.username())
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<Role> extractRoles(String token) {
        Claims claims = parseClaims(token);
        Object raw = claims.get(CLAIM_ROLES);
        if (raw == null) {
            return Set.of();
        }
        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .map(Role::valueOf)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return Set.of();
    }

    public long extractExpirationMillis(String token) {
        Date expiration = parseClaims(token).getExpiration();
        if (expiration == null) {
            return 0L;
        }
        return Math.max(0L, expiration.getTime() - System.currentTimeMillis());
    }

    public boolean isAccessToken(String token) {
        Object type = parseClaims(token).get(CLAIM_TYPE);
        return TYPE_ACCESS.equals(type);
    }

    public boolean isRefreshToken(String token) {
        Object type = parseClaims(token).get(CLAIM_TYPE);
        return TYPE_REFRESH.equals(type);
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = parseClaims(token);
            if (!expectedUsername.equals(claims.getSubject())) {
                return false;
            }
            if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
