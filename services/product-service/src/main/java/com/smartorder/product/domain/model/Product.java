package com.smartorder.product.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Product aggregate root — pure Java domain object.
 *
 * Encapsulates all business rules around product lifecycle,
 * pricing, image management, and approval workflow.
 */
public class Product {

    private final UUID          id;
    private       String        name;
    private       String        description;
    private       String        slug;
    private       Money         price;
    private       Money         compareAtPrice;   // original price for sale display
    private       UUID          categoryId;
    private       UUID          sellerId;
    private       ProductStatus status;
    private       String        rejectionReason;
    private       String        sku;
    private       String        brand;
    private       double        averageRating;
    private       int           reviewCount;
    private       List<ProductImage> images;
    private       List<String>  tags;
    private final Instant       createdAt;
    private       Instant       updatedAt;
    private       String        createdBy;

    // ── Creation constructor ──────────────────────────────────
    public Product(String name,
                   String description,
                   String slug,
                   Money price,
                   UUID categoryId,
                   UUID sellerId,
                   String sku,
                   String brand,
                   List<String> tags) {
        this.id             = UUID.randomUUID();
        this.name           = name;
        this.description    = description;
        this.slug           = slug.toLowerCase().trim();
        this.price          = price;
        this.categoryId     = categoryId;
        this.sellerId       = sellerId;
        this.sku            = sku;
        this.brand          = brand;
        this.tags           = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.images         = new ArrayList<>();
        this.status         = ProductStatus.DRAFT;
        this.averageRating  = 0.0;
        this.reviewCount    = 0;
        this.createdAt      = Instant.now();
        this.updatedAt      = Instant.now();
        this.createdBy      = sellerId.toString();
    }

    // ── Reconstitution constructor ────────────────────────────
    public Product(UUID id, String name, String description,
                   String slug, Money price, Money compareAtPrice,
                   UUID categoryId, UUID sellerId,
                   ProductStatus status, String rejectionReason,
                   String sku, String brand,
                   double averageRating, int reviewCount,
                   List<ProductImage> images, List<String> tags,
                   Instant createdAt, Instant updatedAt, String createdBy) {
        this.id              = id;
        this.name            = name;
        this.description     = description;
        this.slug            = slug;
        this.price           = price;
        this.compareAtPrice  = compareAtPrice;
        this.categoryId      = categoryId;
        this.sellerId        = sellerId;
        this.status          = status;
        this.rejectionReason = rejectionReason;
        this.sku             = sku;
        this.brand           = brand;
        this.averageRating   = averageRating;
        this.reviewCount     = reviewCount;
        this.images          = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.tags            = tags   != null ? new ArrayList<>(tags)   : new ArrayList<>();
        this.createdAt       = createdAt;
        this.updatedAt       = updatedAt;
        this.createdBy       = createdBy;
    }

    // ── Business rules ────────────────────────────────────────

    /**
     * Seller submits draft product for admin review.
     */
    public void submitForReview() {
        if (this.status != ProductStatus.DRAFT
                && this.status != ProductStatus.REJECTED) {
            throw new IllegalStateException(
                    "Only DRAFT or REJECTED products can be submitted for review."
            );
        }
        if (this.images.isEmpty()) {
            throw new IllegalStateException(
                    "Product must have at least one image before submission."
            );
        }
        this.status          = ProductStatus.PENDING;
        this.rejectionReason = null;
        this.updatedAt       = Instant.now();
    }

    /**
     * Admin approves the product — makes it live.
     */
    public void approve() {
        if (this.status != ProductStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING products can be approved."
            );
        }
        this.status    = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Admin rejects the product with a reason.
     */
    public void reject(String reason) {
        if (this.status != ProductStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING products can be rejected."
            );
        }
        this.status          = ProductStatus.REJECTED;
        this.rejectionReason = reason;
        this.updatedAt       = Instant.now();
    }

    /**
     * Seller deactivates a live listing temporarily.
     */
    public void deactivate() {
        if (this.status != ProductStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE products can be deactivated."
            );
        }
        this.status    = ProductStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Seller reactivates a previously deactivated listing.
     */
    public void reactivate() {
        if (this.status != ProductStatus.INACTIVE) {
            throw new IllegalStateException(
                    "Only INACTIVE products can be reactivated."
            );
        }
        this.status    = ProductStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Soft-delete — retains data for order history references.
     */
    public void softDelete() {
        this.status    = ProductStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates core product fields. Only allowed in DRAFT or INACTIVE state.
     */
    public void updateDetails(String name, String description,
                              Money price, Money compareAtPrice,
                              String brand, List<String> tags) {
        if (this.status == ProductStatus.DELETED) {
            throw new IllegalStateException("Cannot update a deleted product.");
        }
        this.name           = name;
        this.description    = description;
        this.price          = price;
        this.compareAtPrice = compareAtPrice;
        this.brand          = brand;
        this.tags           = tags != null ? new ArrayList<>(tags) : this.tags;
        this.updatedAt      = Instant.now();
    }

    /**
     * Adds an image. First image automatically becomes primary.
     */
    public void addImage(ProductImage image) {
        if (this.images.isEmpty()) {
            // First image is always primary
            image = new ProductImage(
                    image.getObjectKey(), image.getUrl(),
                    image.getAltText(), 0, true
            );
        }
        this.images.add(image);
        this.updatedAt = Instant.now();
    }

    /**
     * Removes an image by its ID.
     */
    public void removeImage(UUID imageId) {
        this.images.removeIf(img -> img.getId().equals(imageId));
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the average rating after a new review is submitted.
     */
    public void updateRating(double newAverageRating, int newReviewCount) {
        this.averageRating = newAverageRating;
        this.reviewCount   = newReviewCount;
        this.updatedAt     = Instant.now();
    }

    public boolean isAvailable() {
        return this.status == ProductStatus.ACTIVE;
    }

    public boolean isOwnedBy(UUID sellerId) {
        return this.sellerId.equals(sellerId);
    }

    public ProductImage getPrimaryImage() {
        return images.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    // ── Getters ───────────────────────────────────────────────
    public UUID          getId()              { return id; }
    public String        getName()            { return name; }
    public String        getDescription()     { return description; }
    public String        getSlug()            { return slug; }
    public Money         getPrice()           { return price; }
    public Money         getCompareAtPrice()  { return compareAtPrice; }
    public UUID          getCategoryId()      { return categoryId; }
    public UUID          getSellerId()        { return sellerId; }
    public ProductStatus getStatus()          { return status; }
    public String        getRejectionReason() { return rejectionReason; }
    public String        getSku()             { return sku; }
    public String        getBrand()           { return brand; }
    public double        getAverageRating()   { return averageRating; }
    public int           getReviewCount()     { return reviewCount; }
    public List<ProductImage> getImages()     { return Collections.unmodifiableList(images); }
    public List<String>  getTags()            { return Collections.unmodifiableList(tags); }
    public Instant       getCreatedAt()       { return createdAt; }
    public Instant       getUpdatedAt()       { return updatedAt; }
    public String        getCreatedBy()       { return createdBy; }
}