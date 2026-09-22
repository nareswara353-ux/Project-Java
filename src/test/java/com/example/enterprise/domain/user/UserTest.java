package com.example.enterprise.domain.user;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final UUID ID = UUID.randomUUID();

    @Test
    void constructor_WithValidData_ShouldCreateUser() {
        User user = new User(ID, "admin", "hashed", Set.of(Role.ADMIN), true);

        assertThat(user.id()).isEqualTo(ID);
        assertThat(user.username()).isEqualTo("admin");
        assertThat(user.password()).isEqualTo("hashed");
        assertThat(user.roles()).containsExactly(Role.ADMIN);
        assertThat(user.enabled()).isTrue();
    }

    @Test
    void constructor_WithNullUsername_ShouldThrow() {
        assertThatThrownBy(() -> new User(ID, null, "hashed", Set.of(Role.USER), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void constructor_WithBlankUsername_ShouldThrow() {
        assertThatThrownBy(() -> new User(ID, "  ", "hashed", Set.of(Role.USER), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void constructor_WithNullPassword_ShouldThrow() {
        assertThatThrownBy(() -> new User(ID, "admin", null, Set.of(Role.USER), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
    }

    @Test
    void constructor_WithBlankPassword_ShouldThrow() {
        assertThatThrownBy(() -> new User(ID, "admin", "  ", Set.of(Role.USER), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password");
    }

    @Test
    void constructor_WithNullRoles_ShouldThrow() {
        assertThatThrownBy(() -> new User(ID, "admin", "hashed", null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role");
    }

    @Test
    void constructor_WithEmptyRoles_ShouldThrow() {
        assertThatThrownBy(() -> new User(ID, "admin", "hashed", Set.of(), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role");
    }

    @Test
    void constructor_ShouldMakeRolesImmutable() {
        Set<Role> mutable = new HashSet<>();
        mutable.add(Role.USER);

        User user = new User(ID, "admin", "hashed", mutable, true);
        mutable.add(Role.ADMIN);

        assertThat(user.roles()).containsExactly(Role.USER);
    }

    @Test
    void withPassword_ShouldReturnNewUserWithUpdatedPassword() {
        User original = new User(ID, "admin", "old-hash", Set.of(Role.ADMIN), true);

        User updated = original.withPassword("new-hash");

        assertThat(updated.password()).isEqualTo("new-hash");
        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.username()).isEqualTo(original.username());
        assertThat(updated.roles()).isEqualTo(original.roles());
        assertThat(original.password()).isEqualTo("old-hash");
    }

    @Test
    void withRoles_ShouldReturnNewUserWithUpdatedRoles() {
        User original = new User(ID, "admin", "hashed", Set.of(Role.USER), true);

        User updated = original.withRoles(Set.of(Role.ADMIN, Role.USER));

        assertThat(updated.roles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
        assertThat(updated.password()).isEqualTo(original.password());
        assertThat(original.roles()).containsExactly(Role.USER);
    }

    @Test
    void roleAuthority_ShouldPrefixWithRole() {
        assertThat(Role.ADMIN.authority()).isEqualTo("ROLE_ADMIN");
        assertThat(Role.USER.authority()).isEqualTo("ROLE_USER");
    }
}
