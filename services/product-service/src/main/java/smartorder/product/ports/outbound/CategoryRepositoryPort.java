package com.smartorder.product.ports.outbound;

import com.smartorder.product.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port — persistence contract for Category aggregate.
 */
public interface CategoryRepositoryPort {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    /**
     * Returns all active top-level categories.
     */
    List<Category> findAllActive();

    /**
     * Returns active subcategories of the given parent.
     */
    List<Category> findActiveByParentId(UUID parentId);

    boolean existsBySlug(String slug);

    void deleteById(UUID id);
}