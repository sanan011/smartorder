package com.smartorder.product.adapters.web.dto;

import com.smartorder.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProductResponse {

    private final String     id;
    private final String     name;
    private final String     description;
    private final String     slug;
    private final BigDecimal price;
    private final String     currencyCode;
    private final BigDecimal compareAtPrice;
    private final String     categoryId;
    private final String     sellerId;
    private final String     status;
    private final String     sku;
    private final String     brand;
    private final double     averageRating;
    private final int        reviewCount;
    private final List<ImageDto> images;
    private final List<String>   tags;
    private final Instant    createdAt;
    private final Instant    updatedAt;

    @Getter
    @Builder
    public static class ImageDto {
        private final String  id;
        private final String  url;
        private final String  altText;
        private final int     displayOrder;
        private final boolean primary;
    }

    public static ProductResponse from(Product product) {
        List<ImageDto> images = product.getImages().stream()
                .map(img -> ImageDto.builder()
                        .id(img.getId().toString())
                        .url(img.getUrl())
                        .altText(img.getAltText())
                        .displayOrder(img.getDisplayOrder())
                        .primary(img.isPrimary())
                        .build())
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .description(product.getDescription())
                .slug(product.getSlug())
                .price(product.getPrice().getAmount())
                .currencyCode(product.getPrice().getCurrencyCode())
                .compareAtPrice(product.getCompareAtPrice() != null
                        ? product.getCompareAtPrice().getAmount() : null)
                .categoryId(product.getCategoryId().toString())
                .sellerId(product.getSellerId().toString())
                .status(product.getStatus().name())
                .sku(product.getSku())
                .brand(product.getBrand())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .images(images)
                .tags(product.getTags())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}