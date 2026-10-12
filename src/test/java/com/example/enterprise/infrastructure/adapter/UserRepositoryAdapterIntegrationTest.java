package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.domain.user.Role;
import com.example.enterprise.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterIntegrationTest {

    @Autowired
    private UserRepositoryAdapter repository;

    private User buildUser(String username, Set<Role> roles) {
        return new User(UUID.randomUUID(), username, "hashed-pass", roles, true);
    }

    @Test
    void save_ShouldPersistUserWithRoles() {
        User user = buildUser("admin", Set.of(Role.ADMIN, Role.USER));

        User saved = repository.save(user);

        assertThat(saved.id()).isEqualTo(user.id());
        assertThat(saved.username()).isEqualTo("admin");
        assertThat(saved.roles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
    }

    @Test
    void findById_WhenExists_ShouldReturnUser() {
        User saved = repository.save(buildUser("alice", Set.of(Role.USER)));

        Optional<User> found = repository.findById(saved.id());

        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("alice");
    }

    @Test
    void findById_WhenMissing_ShouldReturnEmpty() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByUsername_WhenExists_ShouldReturnUser() {
        repository.save(buildUser("bob", Set.of(Role.USER)));

        Optional<User> found = repository.findByUsername("bob");

        assertThat(found).isPresent();
        assertThat(found.get().username()).isEqualTo("bob");
    }

    @Test
    void findByUsername_WhenMissing_ShouldReturnEmpty() {
        assertThat(repository.findByUsername("ghost")).isEmpty();
    }

    @Test
    void existsByUsername_ShouldBeTrueForExistingUser() {
        repository.save(buildUser("charlie", Set.of(Role.USER)));

        assertThat(repository.existsByUsername("charlie")).isTrue();
        assertThat(repository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void findAll_ShouldReturnAllPersistedUsers() {
        repository.save(buildUser("u1", Set.of(Role.USER)));
        repository.save(buildUser("u2", Set.of(Role.USER)));

        List<User> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(User::username).containsExactlyInAnyOrder("u1", "u2");
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        User saved = repository.save(buildUser("temp", Set.of(Role.USER)));

        repository.deleteById(saved.id());

        assertThat(repository.existsByUsername("temp")).isFalse();
    }
}
