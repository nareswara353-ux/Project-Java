package com.example.enterprise.config;

import com.example.enterprise.domain.port.UserRepository;
import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDefaultUsers() {
        return args -> {
            seedIfMissing("admin", "admin123", Set.of(Role.ADMIN, Role.USER));
            seedIfMissing("user", "user123", Set.of(Role.USER));
        };
    }

    private void seedIfMissing(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            log.debug("User '{}' already exists, skipping seed", username);
            return;
        }
        User user = new User(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(rawPassword),
                roles,
                true
        );
        userRepository.save(user);
        log.info("Seeded default user: {} with roles {}", username, roles);
    }
}
