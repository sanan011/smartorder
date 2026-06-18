package com.smartorder.product.adapters.persistence.entity;

import com.smartorder.common.audit.AuditMetadata;
import com.smartorder.product.domain.model.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_slug",        columnList = "slug",       unique = true),
                @Index(name = "idx_products_seller_id",   columnList = "seller_id"),
                @Index(name = "idx_products_category_id", columnList = "category_id"),
                @Index(name = "idx_products_status",      columnList = "status"),
                @Index(name = "idx_products_sku_seller",  columnList = "sku,seller_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProductJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "slug", nullable = false, unique = true, length = 300)
    private String slug;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "compare_at_price", precision = 19, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "brand", length = 150)
    private String brand;

    @Column(name = "average_rating", nullable = false)
    private double averageRating;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    // Tags stored as a simple comma-separated string for simplicity.
    // In production consider a join table or JSONB column.
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @OneToMany(
            mappedBy    = "product",
            cascade     = CascadeType.ALL,
            orphanRemoval = true,
            fetch       = FetchType.LAZY
    )
    @OrderBy("displayOrder ASC")
    private List<ProductImageJpaEntity> images = new ArrayList<>();

    @Embedded
    private AuditMetadata audit = new AuditMetadata();
}