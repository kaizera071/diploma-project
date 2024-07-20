package com.audit.system.ingestion.minio;

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
            ensureBucketExists(minioClient, minioBucketName);
        };
    }

    private void ensureBucketExists(MinioClient minioClient, String bucketName) {
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket created successfully.");
            } else {
                System.out.println("Bucket already exists.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
