package com.audit.system.ingestion.minio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@Service
public class MinioService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MinioClient minioClient;

    @Value("${my.minio.bucketname}")
    private String minioBucketName;

    public void saveToMinIO(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            System.out.println(jsonNode.getNodeType());
            String prettyString = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Saving to MinIO: " + prettyString);

            InputStream is = new ByteArrayInputStream(prettyString.getBytes());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object("message-" + System.currentTimeMillis() + ".json")
                            .stream(is, is.available(), -1)
                            .contentType("application/json")
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error saving to MinIO", e);
        }
    }
}
