package com.smartorder.product.ports.outbound;

import java.io.InputStream;

/**
 * Outbound port — object storage contract for product images.
 * The adapter implementation uses MinIO.
 */
public interface ImageStoragePort {

    /**
     * Uploads an image and returns the public-facing URL.
     *
     * @param inputStream  raw image bytes
     * @param objectKey    storage key (e.g. "products/{productId}/{filename}")
     * @param contentType  MIME type (e.g. "image/jpeg")
     * @param sizeBytes    content length in bytes
     * @return public URL of the uploaded image
     */
    String upload(InputStream inputStream,
                  String objectKey,
                  String contentType,
                  long sizeBytes);

    /**
     * Deletes an image by its object key.
     */
    void delete(String objectKey);

    /**
     * Generates a pre-signed URL for temporary direct access.
     * Useful for serving private images without exposing credentials.
     *
     * @param objectKey  storage key
     * @param expirySeconds  how long the URL is valid
     * @return pre-signed URL string
     */
    String generatePresignedUrl(String objectKey, int expirySeconds);

    /**
     * Builds the object key for a product image.
     */
    static String buildObjectKey(String productId, String filename) {
        return "products/" + productId + "/" + filename;
    }
}