package com.smartorder.product.adapters.persistence.entity;

import com.smartorder.common.audit.AuditMetadata;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_slug",      columnList = "slug",      unique = true),
                @Index(name = "idx_categories_parent_id", columnList = "parent_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CategoryJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Embedded
    private AuditMetadata audit = new AuditMetadata();
}