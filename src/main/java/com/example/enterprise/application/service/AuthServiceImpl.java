package com.example.enterprise.application.service;

import com.example.enterprise.application.port.AuthService;
import com.example.enterprise.config.JwtProperties;
import com.example.enterprise.domain.exception.DuplicateProductException;
import com.example.enterprise.domain.port.UserRepository;
import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import com.example.enterprise.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(readOnly = true)
    public AuthTokens login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException ex) {
            log.warn("Login failed for user: {}", username);
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthTokens register(String username, String password, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateProductException("Username '" + username + "' already exists");
        }

        Set<Role> effectiveRoles = (roles == null || roles.isEmpty())
                ? Set.of(Role.USER)
                : Set.copyOf(roles);

        User user = new User(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(password),
                effectiveRoles,
                true
        );

        User saved = userRepository.save(user);
        log.info("Registered new user: {} with roles {}", saved.username(), saved.roles());
        return issueTokens(saved);
    }

    private AuthTokens issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthTokens(
                accessToken,
                refreshToken,
                jwtProperties.getExpirationMs(),
                user.username()
        );
    }
}
