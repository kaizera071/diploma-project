package com.audit.system.forwarder.minio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

@Configuration
public class MinioConfig {

    @Value("${my.minio.url}")
    private String minioUrl;

    @Value("${my.minio.username}")
    private String minioAccessKey;

    @Value("${my.minio.password}")
    private String minioSecretKey;

    @Value("${my.minio.bucketname}")
    private String minioBucketName;

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 5000;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }

    @Bean
    public ApplicationRunner applicationRunner(MinioClient minioClient) {
        return args -> {
            try {
                ensureBucketExists(minioClient, minioBucketName, MAX_RETRIES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Bucket creation operation was interrupted.");
                e.printStackTrace();
            }
        };
    }

    private void ensureBucketExists(MinioClient minioClient, String bucketName, int retriesLeft)
            throws InterruptedException {
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket created successfully.");
            } else {
                System.out.println("Bucket already exists.");
            }
        } catch (Exception e) {
            handleRetry(e, minioClient, bucketName, retriesLeft);
        }
    }

    private void handleRetry(Exception e, MinioClient minioClient, String bucketName, int retriesLeft)
            throws InterruptedException {
        if (retriesLeft <= 0) {
            System.err.println("Failed to ensure bucket existence after maximum attempts.");
            e.printStackTrace();
            throw new RuntimeException("Bucket creation failed after retries", e);
        } else {
            System.out.println("Attempt failed. Retrying in " + (RETRY_DELAY_MS / 1000) + " seconds...");
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw ie;
            }
            ensureBucketExists(minioClient, bucketName, retriesLeft - 1);
        }
    }
}