package com.smartorder.product.adapters.persistence;

import com.smartorder.product.adapters.persistence.entity.CategoryJpaEntity;
import com.smartorder.product.domain.model.Category;
import com.smartorder.product.ports.outbound.CategoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final SpringDataCategoryRepository springDataRepo;

    @Override
    public Category save(Category category) {
        return toDomain(springDataRepo.save(toEntity(category)));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return springDataRepo.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public List<Category> findAllActive() {
        return springDataRepo.findAllByActiveTrue()
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Category> findActiveByParentId(UUID parentId) {
        return springDataRepo.findByParentIdAndActiveTrue(parentId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsBySlug(String slug) {
        return springDataRepo.existsBySlug(slug);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }

    // ── Inline mappers (Category is simple enough) ────────────

    private Category toDomain(CategoryJpaEntity e) {
        return new Category(
                e.getId(), e.getName(), e.getSlug(),
                e.getDescription(), e.getParentId(),
                e.getDisplayOrder(), e.isActive(),
                e.getAudit().getCreatedAt(),
                e.getAudit().getUpdatedAt()
        );
    }

    private CategoryJpaEntity toEntity(Category c) {
        CategoryJpaEntity e = new CategoryJpaEntity();
        e.setId(c.getId());
        e.setName(c.getName());
        e.setSlug(c.getSlug());
        e.setDescription(c.getDescription());
        e.setParentId(c.getParentId());
        e.setDisplayOrder(c.getDisplayOrder());
        e.setActive(c.isActive());
        e.getAudit().setCreatedBy("SYSTEM");
        e.getAudit().setUpdatedBy("SYSTEM");
        return e;
    }
}