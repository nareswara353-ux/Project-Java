package com.example.enterprise.application.service;

import com.example.enterprise.application.port.AuthService;
import com.example.enterprise.config.JwtProperties;
import com.example.enterprise.domain.exception.DuplicateProductException;
import com.example.enterprise.domain.port.UserRepository;
import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import com.example.enterprise.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User(
                UUID.randomUUID(),
                "admin",
                "hashed-password",
                Set.of(Role.ADMIN, Role.USER),
                true
        );
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokens() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", null, Set.of()));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateAccessToken(existingUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(existingUser)).thenReturn("refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3_600_000L);

        AuthService.AuthTokens tokens = authService.login("admin", "admin123");

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.refreshToken()).isEqualTo("refresh-token");
        assertThat(tokens.expiresInMs()).isEqualTo(3_600_000L);
        assertThat(tokens.username()).isEqualTo("admin");
        verify(userRepository).findByUsername("admin");
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowBadCredentials() {
        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login("admin", "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void login_WhenUserDisappearsAfterAuth_ShouldThrowBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", null, Set.of()));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("admin", "admin123"))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void register_WithNewUsername_ShouldSaveWithDefaultUserRole() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("hashed-newpass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3_600_000L);

        AuthService.AuthTokens tokens = authService.register("newuser", "newpass123", null);

        assertThat(tokens.accessToken()).isEqualTo("access-token");
        assertThat(tokens.username()).isEqualTo("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExplicitRoles_ShouldRespectProvidedRoles() {
        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("hashed-newpass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3_600_000L);

        AuthService.AuthTokens tokens = authService.register(
                "newadmin", "newpass123", Set.of(Role.ADMIN, Role.USER));

        assertThat(tokens.username()).isEqualTo("newadmin");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithDuplicateUsername_ShouldThrowException() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.register("admin", "any", null))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateAccessToken(any());
    }
}
