package com.example.enterprise.application.port;

import com.example.enterprise.domain.user.Role;

import java.util.Set;

public interface AuthService {

    record AuthTokens(String accessToken, String refreshToken, long expiresInMs, String username) {}

    AuthTokens login(String username, String password);

    AuthTokens register(String username, String password, Set<Role> roles);
}
