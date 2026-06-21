package com.smartorder.product.adapters.persistence;

import com.smartorder.product.adapters.persistence.entity.ProductJpaEntity;
import com.smartorder.product.domain.model.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {

    Optional<ProductJpaEntity> findBySlugAndStatusNot(
            String slug, ProductStatus status);

    boolean existsBySlug(String slug);

    boolean existsBySkuAndSellerId(String sku, UUID sellerId);

    Optional<ProductJpaEntity> findBySkuAndSellerId(String sku, UUID sellerId);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.sellerId = :sellerId " +
            "AND p.status = :status ORDER BY p.audit.createdAt DESC")
    List<ProductJpaEntity> findBySellerIdAndStatus(
            @Param("sellerId") UUID sellerId,
            @Param("status")   ProductStatus status,
            Pageable pageable);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.categoryId = :categoryId " +
            "AND p.status = 'ACTIVE' ORDER BY p.audit.createdAt DESC")
    List<ProductJpaEntity> findActiveByCategoryId(
            @Param("categoryId") UUID categoryId,
            Pageable pageable);

    @Query("SELECT p FROM ProductJpaEntity p WHERE p.status = :status " +
            "ORDER BY p.audit.createdAt DESC")
    List<ProductJpaEntity> findByStatus(
            @Param("status") ProductStatus status,
            Pageable pageable);

    long countBySellerIdAndStatus(UUID sellerId, ProductStatus status);
}