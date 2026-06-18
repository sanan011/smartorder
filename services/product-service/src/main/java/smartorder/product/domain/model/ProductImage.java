package com.smartorder.product.domain.model;

import java.util.UUID;

/**
 * Value object representing a product image stored in MinIO.
 */
public final class ProductImage {

    private final UUID   id;
    private final String objectKey;      // MinIO object key
    private final String url;            // public-facing CDN/MinIO URL
    private final String altText;
    private final int    displayOrder;   // 0 = primary image
    private final boolean primary;

    public ProductImage(String objectKey,
                        String url,
                        String altText,
                        int displayOrder,
                        boolean primary) {
        this.id           = UUID.randomUUID();
        this.objectKey    = objectKey;
        this.url          = url;
        this.altText      = altText;
        this.displayOrder = displayOrder;
        this.primary      = primary;
    }

    // Reconstitution
    public ProductImage(UUID id, String objectKey, String url,
                        String altText, int displayOrder, boolean primary) {
        this.id           = id;
        this.objectKey    = objectKey;
        this.url          = url;
        this.altText      = altText;
        this.displayOrder = displayOrder;
        this.primary      = primary;
    }

    public UUID    getId()           { return id; }
    public String  getObjectKey()    { return objectKey; }
    public String  getUrl()          { return url; }
    public String  getAltText()      { return altText; }
    public int     getDisplayOrder() { return displayOrder; }
    public boolean isPrimary()       { return primary; }
}