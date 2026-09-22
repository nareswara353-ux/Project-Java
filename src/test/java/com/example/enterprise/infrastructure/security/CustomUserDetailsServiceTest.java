package com.example.enterprise.infrastructure.security;

import com.example.enterprise.domain.port.UserRepository;
import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_WhenExists_ShouldReturnUserDetailsWithAuthorities() {
        User user = new User(
                UUID.randomUUID(),
                "admin",
                "hashed-password",
                Set.of(Role.ADMIN, Role.USER),
                true
        );
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPassword()).isEqualTo("hashed-password");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void loadUserByUsername_WhenUserDisabled_ShouldReturnDisabled() {
        User user = new User(
                UUID.randomUUID(),
                "disabled-user",
                "hashed-password",
                Set.of(Role.USER),
                false
        );
        when(userRepository.findByUsername("disabled-user")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("disabled-user");

        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_WhenNotFound_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}
