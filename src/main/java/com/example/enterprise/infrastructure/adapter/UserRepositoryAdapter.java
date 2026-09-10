package com.example.enterprise.infrastructure.adapter;

import com.example.enterprise.domain.port.UserRepository;
import com.example.enterprise.domain.user.User;
import com.example.enterprise.infrastructure.adapter.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity saved = jpaUserRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(entity -> entity.toDomain());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(entity -> entity.toDomain());
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll().stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }
}
