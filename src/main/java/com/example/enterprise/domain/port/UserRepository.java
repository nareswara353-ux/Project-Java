package com.example.enterprise.domain.port;

import com.example.enterprise.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void deleteById(UUID id);
    boolean existsByUsername(String username);
}
