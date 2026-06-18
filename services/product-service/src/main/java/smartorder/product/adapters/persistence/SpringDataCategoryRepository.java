package com.smartorder.product.adapters.persistence;

import com.smartorder.product.adapters.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    Optional<CategoryJpaEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<CategoryJpaEntity> findAllByActiveTrue();

    List<CategoryJpaEntity> findByParentIdAndActiveTrue(UUID parentId);
}