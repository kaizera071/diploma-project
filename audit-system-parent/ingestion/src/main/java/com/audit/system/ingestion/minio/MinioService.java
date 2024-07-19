package com.audit.system.ingestion.minio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${my.minio.bucketname}")
    private String minioBucketName;

    public void saveToMinIO(String message) {
        try {
            InputStream is = new ByteArrayInputStream(message.getBytes());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object("message-" + System.currentTimeMillis() + ".txt")
                            .stream(is, is.available(), -1)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error saving to MinIO", e);
        }
    }
}
