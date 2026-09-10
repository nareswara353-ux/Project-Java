package com.example.enterprise.domain.user;

import java.util.Set;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String password,
        Set<Role> roles,
        boolean enabled
) {
    public User {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
        roles = Set.copyOf(roles);
    }

    public User withPassword(String newPassword) {
        return new User(this.id, this.username, newPassword, this.roles, this.enabled);
    }

    public User withRoles(Set<Role> newRoles) {
        return new User(this.id, this.username, this.password, newRoles, this.enabled);
    }
}
