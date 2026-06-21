package com.smartorder.product.adapters.web;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.product.adapters.web.dto.CreateProductRequest;
import com.smartorder.product.adapters.web.dto.ProductResponse;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.ports.inbound.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase          createProductUseCase;
    private final UpdateProductUseCase          updateProductUseCase;
    private final SubmitProductForReviewUseCase submitForReviewUseCase;
    private final ApproveProductUseCase         approveProductUseCase;
    private final RejectProductUseCase          rejectProductUseCase;
    private final DeleteProductUseCase          deleteProductUseCase;
    private final GetProductUseCase             getProductUseCase;
    private final SearchProductsUseCase         searchProductsUseCase;
    private final UploadProductImageUseCase     uploadImageUseCase;

    // ── GET /api/v1/products/{id} ─────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        Product product = getProductUseCase.findById(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // ── GET /api/v1/products/slug/{slug} ──────────────────────

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        Product product = getProductUseCase.findBySlug(slug);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    // ── GET /api/v1/products?category=&page=&size= ────────────

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(
            @RequestParam(required = false) UUID category,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        List<Product> products = category != null
                ? getProductUseCase.findByCategory(category, page, size)
                : searchProductsUseCase.search(
                new SearchProductsUseCase.Query(
                        null, null, null, null, "newest", page, size
                )).products();

        return ResponseEntity.ok(products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList()));
    }

    // ── GET /api/v1/products/search?q=&category=&... ─────────

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchProductsUseCase.Result result = searchProductsUseCase.search(
                new SearchProductsUseCase.Query(
                        q, category, minPrice, maxPrice, sortBy, page, size
                )
        );

        return ResponseEntity.ok(Map.of(
                "products",   result.products().stream()
                        .map(ProductResponse::from).collect(Collectors.toList()),
                "totalHits",  result.totalHits(),
                "page",       result.page(),
                "size",       result.size(),
                "totalPages", result.totalPages()
        ));
    }

    // ── GET /api/v1/products/autocomplete?prefix= ─────────────

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String prefix) {
        return ResponseEntity.ok(searchProductsUseCase.autocomplete(prefix));
    }

    // ── POST /api/v1/products ─────────────────────────────────

    @PostMapping
    public ResponseEntity<Map<String, String>> create(
            @Valid @RequestBody CreateProductRequest request,
            HttpServletRequest httpRequest) {

        UUID sellerId = extractUserId(httpRequest);

        CreateProductUseCase.Result result = createProductUseCase.execute(
                new CreateProductUseCase.Command(
                        request.getName(),
                        request.getDescription(),
                        request.getPrice(),
                        request.getCurrencyCode(),
                        request.getCompareAtPrice(),
                        request.getCategoryId(),
                        sellerId,
                        request.getSku(),
                        request.getBrand(),
                        request.getTags()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "productId", result.productId(),
                "slug",      result.slug(),
                "status",    result.status()
        ));
    }

    // ── POST /api/v1/products/{id}/images ─────────────────────

    @PostMapping(value = "/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) throws Exception {

        UUID sellerId = extractUserId(httpRequest);

        UploadProductImageUseCase.Result result = uploadImageUseCase.execute(
                new UploadProductImageUseCase.Command(
                        id,
                        sellerId,
                        file.getInputStream(),
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getSize()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "imageId",   result.imageId(),
                "url",       result.url(),
                "isPrimary", result.isPrimary()
        ));
    }

    // ── POST /api/v1/products/{id}/submit ─────────────────────

    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submitForReview(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID sellerId = extractUserId(httpRequest);
        submitForReviewUseCase.execute(
                new SubmitProductForReviewUseCase.Command(id, sellerId)
        );
        return ResponseEntity.noContent().build();
    }

    // ── POST /api/v1/products/{id}/approve (ADMIN) ────────────

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        assertRole(httpRequest, "ADMIN");
        UUID adminId = extractUserId(httpRequest);
        approveProductUseCase.execute(
                new ApproveProductUseCase.Command(id, adminId)
        );
        return ResponseEntity.noContent().build();
    }

    // ── POST /api/v1/products/{id}/reject (ADMIN) ─────────────

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {

        assertRole(httpRequest, "ADMIN");
        UUID adminId = extractUserId(httpRequest);
        rejectProductUseCase.execute(
                new RejectProductUseCase.Command(id, adminId, body.get("reason"))
        );
        return ResponseEntity.noContent().build();
    }

    // ── DELETE /api/v1/products/{id} ──────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID   requesterId = extractUserId(httpRequest);
        String role        = httpRequest.getHeader("X-Auth-Role");
        boolean isAdmin    = "ADMIN".equals(role);

        deleteProductUseCase.execute(
                new DeleteProductUseCase.Command(id, requesterId, isAdmin)
        );
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────

    private UUID extractUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-Auth-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }
        return UUID.fromString(userId);
    }

    private void assertRole(HttpServletRequest request, String requiredRole) {
        String role = request.getHeader("X-Auth-Role");
        if (!requiredRole.equals(role)) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }
    }
}