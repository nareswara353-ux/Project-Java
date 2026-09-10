package com.example.enterprise.interfaces.rest;

import com.example.enterprise.application.port.AuthService;
import com.example.enterprise.domain.port.UserRepository;
import com.example.enterprise.domain.user.User;
import com.example.enterprise.infrastructure.security.JwtService;
import com.example.enterprise.interfaces.rest.dto.AuthResponse;
import com.example.enterprise.interfaces.rest.dto.LoginRequest;
import com.example.enterprise.interfaces.rest.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthService.AuthTokens tokens = authService.register(
                request.username(),
                request.password(),
                request.roles()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AuthResponse.of(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInMs(), tokens.username())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthTokens tokens = authService.login(request.username(), request.password());
        return ResponseEntity.ok(
                AuthResponse.of(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInMs(), tokens.username())
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        if (!jwtService.isTokenValid(refreshToken, username)) {
            throw new BadCredentialsException("Refresh token expired or invalid");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User no longer exists"));

        String newAccessToken = jwtService.generateAccessToken(user);
        long expiresInMs = jwtService.extractExpirationMillis(newAccessToken);

        return ResponseEntity.ok(AuthResponse.of(
                newAccessToken,
                refreshToken,
                expiresInMs,
                user.username()
        ));
    }
}
