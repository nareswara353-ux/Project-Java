package com.example.enterprise.infrastructure.adapter.entity;

import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEntityMappingTest {

    @Test
    void fromDomain_ShouldCopyAllFields() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "admin", "hashed-pass", Set.of(Role.ADMIN, Role.USER), true);

        UserEntity entity = UserEntity.fromDomain(user);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUsername()).isEqualTo("admin");
        assertThat(entity.getPassword()).isEqualTo("hashed-pass");
        assertThat(entity.getRoles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
        assertThat(entity.isEnabled()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(0L);
    }

    @Test
    void toDomain_ShouldCopyAllFields() {
        UUID id = UUID.randomUUID();
        UserEntity entity = UserEntity.builder()
                .id(id)
                .username("alice")
                .password("hashed")
                .roles(Set.of(Role.USER))
                .enabled(false)
                .build();

        User user = entity.toDomain();

        assertThat(user.id()).isEqualTo(id);
        assertThat(user.username()).isEqualTo("alice");
        assertThat(user.password()).isEqualTo("hashed");
        assertThat(user.roles()).containsExactly(Role.USER);
        assertThat(user.enabled()).isFalse();
    }

    @Test
    void roundtrip_ShouldPreserveData() {
        User original = new User(UUID.randomUUID(), "bob", "hash", Set.of(Role.USER), true);

        User roundtrip = UserEntity.fromDomain(original).toDomain();

        assertThat(roundtrip.id()).isEqualTo(original.id());
        assertThat(roundtrip.username()).isEqualTo(original.username());
        assertThat(roundtrip.password()).isEqualTo(original.password());
        assertThat(roundtrip.roles()).isEqualTo(original.roles());
        assertThat(roundtrip.enabled()).isEqualTo(original.enabled());
    }

    @Test
    void toDomain_WithEmptyRoles_ShouldThrow() {
        UserEntity entity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("no-role")
                .password("hash")
                .roles(Set.of())
                .enabled(true)
                .build();

        assertThatThrownBy(entity::toDomain)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role");
    }
}
