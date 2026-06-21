package com.smartorder.product.domain.service;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.domain.model.ProductImage;
import com.smartorder.product.ports.inbound.UploadProductImageUseCase;
import com.smartorder.product.ports.outbound.ImageStoragePort;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import com.smartorder.product.ports.outbound.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class UploadProductImageService implements UploadProductImageUseCase {

    private static final long   MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024L; // 5 MB
    private static final int    MAX_IMAGES_PER_PRODUCT = 10;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final ProductRepositoryPort productRepository;
    private final ImageStoragePort      imageStorage;
    private final ProductSearchPort     searchPort;

    @Override
    public Result execute(Command command) {
        // ── Load and validate product ────────────────────────
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new SmartOrderException(
                        ErrorCode.PRODUCT_NOT_FOUND,
                        "productId=" + command.productId()
                ));

        if (!product.isOwnedBy(command.sellerId())) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }

        // ── Validate image constraints ───────────────────────
        if (command.sizeBytes() > MAX_IMAGE_SIZE_BYTES) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Image exceeds maximum size of 5MB."
            );
        }

        if (!ALLOWED_TYPES.contains(command.contentType())) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Unsupported image type. Allowed: JPEG, PNG, WebP."
            );
        }

        if (product.getImages().size() >= MAX_IMAGES_PER_PRODUCT) {
            throw new SmartOrderException(
                    ErrorCode.VALIDATION_FAILED,
                    "Maximum of " + MAX_IMAGES_PER_PRODUCT + " images per product."
            );
        }

        // ── Build object key and upload ──────────────────────
        String ext       = extractExtension(command.originalFilename());
        String imageId   = UUID.randomUUID().toString();
        String objectKey = ImageStoragePort.buildObjectKey(
                command.productId().toString(),
                imageId + "." + ext
        );

        String url;
        try {
            url = imageStorage.upload(
                    command.imageStream(),
                    objectKey,
                    command.contentType(),
                    command.sizeBytes()
            );
        } catch (Exception e) {
            log.error("MinIO upload failed: {}", e.getMessage());
            throw new SmartOrderException(
                    ErrorCode.IMAGE_UPLOAD_FAILED,
                    e.getMessage()
            );
        }

        // ── Add image to product aggregate ───────────────────
        boolean isPrimary = product.getImages().isEmpty();
        ProductImage image = new ProductImage(
                objectKey, url,
                command.originalFilename(),
                product.getImages().size(),
                isPrimary
        );

        product.addImage(image);
        Product saved = productRepository.save(product);

        // ── Re-index (primary image URL changed) ─────────────
        try {
            searchPort.indexProduct(saved);
        } catch (Exception e) {
            log.warn("Failed to re-index after image upload: {}", e.getMessage());
        }

        log.info("Image uploaded: productId={}, objectKey={}", command.productId(), objectKey);

        return new Result(imageId, url, isPrimary);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}