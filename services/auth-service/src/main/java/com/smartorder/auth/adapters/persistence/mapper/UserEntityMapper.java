package com.smartorder.auth.adapters.persistence.mapper;

import com.smartorder.auth.adapters.persistence.entity.UserJpaEntity;
import com.smartorder.auth.domain.model.User;
import org.springframework.stereotype.Component;

/**
 * Manual mapper between the JPA entity and the domain model.
 * We use a manual mapper (rather than MapStruct) here because the
 * domain User has no no-arg constructor and uses a reconstitution
 * constructor — MapStruct cannot handle this cleanly without
 * complex configuration.
 */
@Component
public class UserEntityMapper {

    /**
     * Converts a domain {@link User} → {@link UserJpaEntity} for persistence.
     */
    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setRole(user.getRole());
        entity.setStatus(user.getStatus());
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLockedUntil(user.getLockedUntil());

        // Sync audit metadata from domain object
        entity.getAudit().setCreatedBy(user.getCreatedBy());
        entity.getAudit().setUpdatedBy(user.getCreatedBy());

        return entity;
    }

    /**
     * Converts a {@link UserJpaEntity} → domain {@link User} for use-case layer.
     * Uses the reconstitution constructor to preserve all state.
     */
    public User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getRole(),
                entity.getStatus(),
                entity.getFailedLoginAttempts(),
                entity.getLockedUntil(),
                entity.getAudit().getCreatedAt(),
                entity.getAudit().getUpdatedAt(),
                entity.getAudit().getCreatedBy()
        );
    }
}