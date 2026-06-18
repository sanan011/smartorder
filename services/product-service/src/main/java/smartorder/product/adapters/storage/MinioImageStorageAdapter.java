package com.smartorder.product.adapters.storage;

import com.smartorder.product.ports.outbound.ImageStoragePort;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioImageStorageAdapter implements ImageStoragePort {

    private final MinioClient minioClient;
    private final String      bucketName;
    private final String      publicEndpoint;

    public MinioImageStorageAdapter(
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName,
            @Value("${minio.endpoint}")    String publicEndpoint) {
        this.minioClient    = minioClient;
        this.bucketName     = bucketName;
        this.publicEndpoint = publicEndpoint;
    }

    @Override
    public String upload(InputStream inputStream,
                         String objectKey,
                         String contentType,
                         long sizeBytes) {
        try {
            // Ensure bucket exists
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Created MinIO bucket: {}", bucketName);
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(inputStream, sizeBytes, -1)
                            .contentType(contentType)
                            .build()
            );

            String url = publicEndpoint + "/" + bucketName + "/" + objectKey;
            log.debug("Uploaded image to MinIO: {}", url);
            return url;

        } catch (Exception e) {
            log.error("MinIO upload failed for key={}: {}", objectKey, e.getMessage());
            throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
            log.debug("Deleted image from MinIO: {}", objectKey);
        } catch (Exception e) {
            log.error("MinIO delete failed for key={}: {}", objectKey, e.getMessage());
            throw new RuntimeException("Image delete failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, int expirySeconds) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectKey)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for key={}: {}",
                    objectKey, e.getMessage());
            throw new RuntimeException("Presigned URL generation failed", e);
        }
    }
}