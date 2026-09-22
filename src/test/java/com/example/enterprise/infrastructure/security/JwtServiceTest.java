package com.example.enterprise.infrastructure.security;

import com.example.enterprise.config.JwtProperties;
import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("Y2hhbmdlLW1lLWluLXByb2R1Y3Rpb24tMTIzNDU2Nzg5MC1hYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5eg==");
        props.setExpirationMs(3_600_000L);
        props.setRefreshExpirationMs(86_400_000L);
        props.setIssuer("java-enterprise-portfolio");

        jwtService = new JwtService(props);

        testUser = new User(
                UUID.randomUUID(),
                "admin",
                "hashed-password",
                Set.of(Role.ADMIN, Role.USER),
                true
        );
    }

    @Test
    void generateAccessToken_ShouldContainUsernameAndRoles() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.extractRoles(token)).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
    }

    @Test
    void generateAccessToken_ShouldBeAccessToken() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(jwtService.isAccessToken(token)).isTrue();
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void generateRefreshToken_ShouldBeRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);

        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.isAccessToken(token)).isFalse();
    }

    @Test
    void extractUsername_ShouldReturnSubject() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void extractExpirationMillis_ShouldReturnPositiveValue() {
        String token = jwtService.generateAccessToken(testUser);

        long millis = jwtService.extractExpirationMillis(token);

        assertThat(millis).isPositive();
        assertThat(millis).isLessThanOrEqualTo(3_600_000L);
    }

    @Test
    void isTokenValid_WithCorrectUsername_ShouldReturnTrue() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(jwtService.isTokenValid(token, "admin")).isTrue();
    }

    @Test
    void isTokenValid_WithWrongUsername_ShouldReturnFalse() {
        String token = jwtService.generateAccessToken(testUser);

        assertThat(jwtService.isTokenValid(token, "someone-else")).isFalse();
    }

    @Test
    void isTokenValid_WithTamperedToken_ShouldReturnFalse() {
        String token = jwtService.generateAccessToken(testUser);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isTokenValid(tampered, "admin")).isFalse();
    }

    @Test
    void isTokenValid_WithGarbageToken_ShouldReturnFalse() {
        assertThat(jwtService.isTokenValid("not-a-jwt", "admin")).isFalse();
    }

    @Test
    void extractRoles_FromRefreshToken_ShouldReturnEmpty() {
        String token = jwtService.generateRefreshToken(testUser);

        assertThat(jwtService.extractRoles(token)).isEmpty();
    }
}
