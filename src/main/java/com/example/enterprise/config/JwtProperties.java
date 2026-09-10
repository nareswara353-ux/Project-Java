package com.example.enterprise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long expirationMs;
    private long refreshExpirationMs;
    private String issuer;
}
