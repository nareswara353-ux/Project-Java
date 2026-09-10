package com.example.enterprise.interfaces.rest.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String username
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInMs, String username) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresInMs / 1000,
                username
        );
    }
}
