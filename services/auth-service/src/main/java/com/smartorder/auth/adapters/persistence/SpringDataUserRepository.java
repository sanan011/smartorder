package com.smartorder.auth.adapters.persistence;

import com.smartorder.auth.adapters.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository — infrastructure detail.
 * Only visible to the adapter layer, never to domain or use cases.
 */
interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    @Query("SELECT u FROM UserJpaEntity u WHERE LOWER(u.email) = LOWER(:email) AND u.status <> 'DELETED'")
    Optional<UserJpaEntity> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM UserJpaEntity u WHERE LOWER(u.email) = LOWER(:email) AND u.status <> 'DELETED'")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
}