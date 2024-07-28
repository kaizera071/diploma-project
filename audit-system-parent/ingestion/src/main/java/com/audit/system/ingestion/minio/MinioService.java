package com.audit.system.ingestion.minio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

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
            String eventType = "";
            String tenant = "";
            String time = "";
            String user = "";

            JsonNode jsonNode = objectMapper.readTree(message);
            JsonNode auditEventNode = jsonNode.path("audit_event");

            Iterator<Map.Entry<String, JsonNode>> fields = auditEventNode.fields();

            if (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                eventType = field.getKey();
                JsonNode eventData = field.getValue();

                tenant = eventData.path("tenant").asText();
                time = eventData.path("time").asText();
                user = eventData.path("user").asText();
            }

            String prettyString = objectMapper.writeValueAsString(jsonNode);
            String objectKey = String.format("%s/%s/%s/%s", tenant, time, eventType, user);

            InputStream is = new ByteArrayInputStream(prettyString.getBytes());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(objectKey + "/message-" + System.currentTimeMillis() + ".json")
                            .stream(is, is.available(), -1)
                            .contentType("application/json")
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error saving to MinIO", e);
        }
    }
}
