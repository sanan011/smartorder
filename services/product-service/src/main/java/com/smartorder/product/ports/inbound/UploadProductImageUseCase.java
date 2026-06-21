package com.smartorder.product.ports.inbound;

import java.io.InputStream;
import java.util.UUID;

/**
 * Inbound port — upload a product image to MinIO.
 */
public interface UploadProductImageUseCase {

    Result execute(Command command);

    record Command(
            UUID        productId,
            UUID        sellerId,
            InputStream imageStream,
            String      originalFilename,
            String      contentType,
            long        sizeBytes
    ) {}

    record Result(
            String imageId,
            String url,
            boolean isPrimary
    ) {}
}