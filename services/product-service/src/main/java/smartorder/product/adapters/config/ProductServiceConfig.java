package com.smartorder.product.adapters.config;

import com.smartorder.product.adapters.messaging.ProductEventPublisherAdapter;
import com.smartorder.product.adapters.persistence.CategoryRepositoryAdapter;
import com.smartorder.product.adapters.persistence.ProductRepositoryAdapter;
import com.smartorder.product.adapters.search.ElasticsearchProductSearchAdapter;
import com.smartorder.product.adapters.storage.MinioImageStorageAdapter;
import com.smartorder.product.domain.service.*;
import com.smartorder.common.filter.CorrelationIdFilter;
import io.minio.MinioClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ProductServiceConfig {

    // ── MinIO client ──────────────────────────────────────────

    @Bean
    public MinioClient minioClient(
            @Value("${minio.endpoint}")   String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    // ── Use-case beans (composition root) ────────────────────

    @Bean
    public CreateProductService createProductService(
            ProductRepositoryAdapter     productRepository,
            CategoryRepositoryAdapter    categoryRepository,
            ProductEventPublisherAdapter eventPublisher,
            ElasticsearchProductSearchAdapter searchPort) {
        return new CreateProductService(
                productRepository, categoryRepository,
                eventPublisher, searchPort
        );
    }

    @Bean
    public UpdateProductService updateProductService(
            ProductRepositoryAdapter     productRepository,
            ProductEventPublisherAdapter eventPublisher,
            ElasticsearchProductSearchAdapter searchPort) {
        return new UpdateProductService(productRepository, eventPublisher, searchPort);
    }

    @Bean
    public ReviewProductService reviewProductService(
            ProductRepositoryAdapter     productRepository,
            ProductEventPublisherAdapter eventPublisher,
            ElasticsearchProductSearchAdapter searchPort) {
        return new ReviewProductService(productRepository, eventPublisher, searchPort);
    }

    @Bean
    public DeleteProductService deleteProductService(
            ProductRepositoryAdapter     productRepository,
            ProductEventPublisherAdapter eventPublisher,
            ElasticsearchProductSearchAdapter searchPort) {
        return new DeleteProductService(productRepository, eventPublisher, searchPort);
    }

    @Bean
    public GetProductService getProductService(
            ProductRepositoryAdapter productRepository) {
        return new GetProductService(productRepository);
    }

    @Bean
    public SearchProductsService searchProductsService(
            ElasticsearchProductSearchAdapter searchPort,
            ProductRepositoryAdapter          productRepository) {
        return new SearchProductsService(searchPort, productRepository);
    }

    @Bean
    public UploadProductImageService uploadProductImageService(
            ProductRepositoryAdapter          productRepository,
            MinioImageStorageAdapter          imageStorage,
            ElasticsearchProductSearchAdapter searchPort) {
        return new UploadProductImageService(productRepository, imageStorage, searchPort);
    }

    // ── Kafka topics ──────────────────────────────────────────

    @Bean public NewTopic productCreatedTopic() {
        return TopicBuilder.name("smartorder.product.created").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic productUpdatedTopic() {
        return TopicBuilder.name("smartorder.product.updated").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic productApprovedTopic() {
        return TopicBuilder.name("smartorder.product.approved").partitions(1).replicas(1).build();
    }
    @Bean public NewTopic productRejectedTopic() {
        return TopicBuilder.name("smartorder.product.rejected").partitions(1).replicas(1).build();
    }
    @Bean public NewTopic productDeletedTopic() {
        return TopicBuilder.name("smartorder.product.deleted").partitions(1).replicas(1).build();
    }

    // ── Correlation filter ────────────────────────────────────

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationFilter() {
        FilterRegistrationBean<CorrelationIdFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new CorrelationIdFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(1);
        return reg;
    }
}