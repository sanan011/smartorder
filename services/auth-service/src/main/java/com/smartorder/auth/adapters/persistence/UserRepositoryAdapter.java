package com.smartorder.auth.adapters.persistence;

import com.smartorder.auth.adapters.persistence.mapper.UserEntityMapper;
import com.smartorder.auth.domain.model.User;
import com.smartorder.auth.ports.outbound.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the outbound {@link UserRepositoryPort}
 * using Spring Data JPA. This is the only place where JPA
 * concepts cross into the port/adapter boundary.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataRepo;
    private final UserEntityMapper         mapper;

    @Override
    public User save(User user) {
        var entity = mapper.toEntity(user);
        var saved  = springDataRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepo.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepo.findByEmailIgnoreCase(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepo.existsByEmailIgnoreCase(email);
    }

    @Override
    public void hardDeleteById(UUID id) {
        springDataRepo.deleteById(id);
    }
}