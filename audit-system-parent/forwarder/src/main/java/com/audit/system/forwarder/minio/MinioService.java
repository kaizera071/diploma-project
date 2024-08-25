package com.audit.system.forwarder.minio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.audit.system.forwarder.security.EncryptionUtil;
import com.audit.system.forwarder.security.KeyManager;
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

    @Autowired
    private KeyManager keyManager;

    @Value("${my.minio.bucketname}")
    private String minioBucketName;

    private String eventType;
    private String tenant;
    private String time;
    private String user;

    public void saveToMinIO(String message) {
        try {
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
            String objectKey = String.format("%s/%s/%s/%s", tenant, time, eventType, user);
            String prettyString = objectMapper.writeValueAsString(jsonNode);
            String encryptedMessage = EncryptionUtil.encrypt(prettyString, keyManager.getSecretKey());
            InputStream is = new ByteArrayInputStream(encryptedMessage.getBytes());

            minioClient.putObject(PutObjectArgs.builder()
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
