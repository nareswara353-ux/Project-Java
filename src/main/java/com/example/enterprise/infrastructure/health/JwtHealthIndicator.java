package com.example.enterprise.infrastructure.health;

import com.example.enterprise.config.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtHealthIndicator implements HealthIndicator {

    private static final int MIN_SECRET_BYTES = 32;

    private final JwtProperties jwtProperties;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();

        if (jwtProperties.getSecret() == null || jwtProperties.getSecret().isBlank()) {
            return Health.down()
                    .withDetail("reason", "JWT secret is missing")
                    .build();
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("reason", "JWT secret is not valid Base64")
                    .withDetail("error", ex.getClass().getSimpleName())
                    .build();
        }

        if (keyBytes.length < MIN_SECRET_BYTES) {
            return Health.down()
                    .withDetail("reason", "JWT secret too short for HS256")
                    .withDetail("actualBytes", keyBytes.length)
                    .withDetail("requiredBytes", MIN_SECRET_BYTES)
                    .build();
        }

        if (jwtProperties.getExpirationMs() <= 0) {
            return Health.down()
                    .withDetail("reason", "JWT access token expiration must be positive")
                    .withDetail("expirationMs", jwtProperties.getExpirationMs())
                    .build();
        }

        if (jwtProperties.getRefreshExpirationMs() <= 0) {
            return Health.down()
                    .withDetail("reason", "JWT refresh token expiration must be positive")
                    .withDetail("refreshExpirationMs", jwtProperties.getRefreshExpirationMs())
                    .build();
        }

        if (jwtProperties.getIssuer() == null || jwtProperties.getIssuer().isBlank()) {
            return Health.down()
                    .withDetail("reason", "JWT issuer is missing")
                    .build();
        }

        return builder
                .withDetail("secretLengthBytes", keyBytes.length)
                .withDetail("expirationMs", jwtProperties.getExpirationMs())
                .withDetail("refreshExpirationMs", jwtProperties.getRefreshExpirationMs())
                .withDetail("issuer", jwtProperties.getIssuer())
                .build();
    }
}
