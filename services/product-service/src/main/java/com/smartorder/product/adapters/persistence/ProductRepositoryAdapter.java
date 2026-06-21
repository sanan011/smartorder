package com.smartorder.product.adapters.persistence;

import com.smartorder.product.adapters.persistence.entity.ProductImageJpaEntity;
import com.smartorder.product.adapters.persistence.entity.ProductJpaEntity;
import com.smartorder.product.adapters.persistence.mapper.ProductEntityMapper;
import com.smartorder.product.domain.model.Product;
import com.smartorder.product.domain.model.ProductStatus;
import com.smartorder.product.ports.outbound.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// open-in-view is disabled and the domain services are not transactional, so
// entity->domain mapping (which touches the LAZY images collection) must run
// inside a transaction here or it throws LazyInitializationException.
@Component
@RequiredArgsConstructor
@Transactional
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository springDataRepo;
    private final ProductEntityMapper         mapper;

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toEntity(product);

        // Sync image children
        entity.getImages().clear();
        product.getImages().forEach(img ->
                entity.getImages().add(mapper.imageToEntity(img, entity))
        );

        return mapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return springDataRepo
                .findBySlugAndStatusNot(slug, ProductStatus.DELETED)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySkuAndSellerId(String sku, UUID sellerId) {
        return springDataRepo.findBySkuAndSellerId(sku, sellerId).map(mapper::toDomain);
    }

    @Override
    public List<Product> findBySellerId(UUID sellerId,
                                        ProductStatus status,
                                        int page, int size) {
        return springDataRepo
                .findBySellerIdAndStatus(sellerId, status, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Product> findByCategoryId(UUID categoryId, int page, int size) {
        return springDataRepo
                .findActiveByCategoryId(categoryId, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Product> findByStatus(ProductStatus status, int page, int size) {
        return springDataRepo
                .findByStatus(status, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countBySellerId(UUID sellerId, ProductStatus status) {
        return springDataRepo.countBySellerIdAndStatus(sellerId, status);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return springDataRepo.existsBySlug(slug);
    }

    @Override
    public boolean existsBySkuAndSellerId(String sku, UUID sellerId) {
        return springDataRepo.existsBySkuAndSellerId(sku, sellerId);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }
}