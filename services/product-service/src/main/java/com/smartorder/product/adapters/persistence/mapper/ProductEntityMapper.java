package com.smartorder.product.adapters.persistence.mapper;

import com.smartorder.product.adapters.persistence.entity.ProductImageJpaEntity;
import com.smartorder.product.adapters.persistence.entity.ProductJpaEntity;
import com.smartorder.product.domain.model.Money;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.domain.model.ProductImage;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductEntityMapper {

    public ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setSlug(product.getSlug());
        entity.setPrice(product.getPrice().getAmount());
        entity.setCurrencyCode(product.getPrice().getCurrencyCode());
        entity.setCompareAtPrice(
                product.getCompareAtPrice() != null
                        ? product.getCompareAtPrice().getAmount() : null
        );
        entity.setCategoryId(product.getCategoryId());
        entity.setSellerId(product.getSellerId());
        entity.setStatus(product.getStatus());
        entity.setRejectionReason(product.getRejectionReason());
        entity.setSku(product.getSku());
        entity.setBrand(product.getBrand());
        entity.setAverageRating(product.getAverageRating());
        entity.setReviewCount(product.getReviewCount());
        entity.setTags(
                product.getTags() != null
                        ? String.join(",", product.getTags()) : ""
        );
        entity.getAudit().setCreatedBy(product.getCreatedBy());
        entity.getAudit().setUpdatedBy(product.getCreatedBy());
        return entity;
    }

    public Product toDomain(ProductJpaEntity entity) {
        List<ProductImage> images = entity.getImages().stream()
                .map(this::imageToDomain)
                .collect(Collectors.toList());

        List<String> tags = (entity.getTags() != null && !entity.getTags().isBlank())
                ? Arrays.asList(entity.getTags().split(","))
                : List.of();

        Money price = new Money(entity.getPrice(), entity.getCurrencyCode());
        Money compareAt = entity.getCompareAtPrice() != null
                ? new Money(entity.getCompareAtPrice(), entity.getCurrencyCode())
                : null;

        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSlug(),
                price,
                compareAt,
                entity.getCategoryId(),
                entity.getSellerId(),
                entity.getStatus(),
                entity.getRejectionReason(),
                entity.getSku(),
                entity.getBrand(),
                entity.getAverageRating(),
                entity.getReviewCount(),
                images,
                tags,
                entity.getAudit().getCreatedAt(),
                entity.getAudit().getUpdatedAt(),
                entity.getAudit().getCreatedBy()
        );
    }

    private ProductImage imageToDomain(ProductImageJpaEntity e) {
        return new ProductImage(
                e.getId(),
                e.getObjectKey(),
                e.getUrl(),
                e.getAltText(),
                e.getDisplayOrder(),
                e.isPrimaryImage()
        );
    }

    public ProductImageJpaEntity imageToEntity(
            ProductImage image, ProductJpaEntity productEntity) {
        ProductImageJpaEntity e = new ProductImageJpaEntity();
        e.setId(image.getId());
        e.setProduct(productEntity);
        e.setObjectKey(image.getObjectKey());
        e.setUrl(image.getUrl());
        e.setAltText(image.getAltText());
        e.setDisplayOrder(image.getDisplayOrder());
        e.setPrimaryImage(image.isPrimary());
        return e;
    }
}