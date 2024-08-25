package com.audit.system.retrieval.minio;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.audit.system.retrieval.security.EncryptionUtil;
import com.audit.system.retrieval.security.KeyManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KeyManager keyManager;

    public List<String> listObjects(String bucketName, String prefix) {
        List<String> objectNames = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build());
            for (Result<Item> result : results) {
                objectNames.add(result.get().objectName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectNames;
    }

    public JsonNode readObject(String bucketName, String objectName) {
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            // Since the data is encrypted, we decrypt it here
            String encryptedContent = new String(stream.readAllBytes());
            String decryptedContent = decryptMessage(encryptedContent);
            return objectMapper.readTree(decryptedContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<JsonNode> searchObjects(String bucketName, String tenant, String eventType, String user,
            Instant startTime, Instant endTime) {
        String prefix = createPrefix(tenant);
        List<String> objectNames = listObjects(bucketName, prefix);

        return objectNames.stream()
                .filter(objectName -> isValidObject(objectName, startTime, endTime, eventType, user))
                .map(objectName -> readObject(bucketName, objectName))
                .collect(Collectors.toList());
    }

    private String createPrefix(String tenant) {
        if (tenant != null && !tenant.isEmpty()) {
            return tenant + "/";
        }
        return "";
    }

    private boolean isValidObject(String objectName, Instant startTime, Instant endTime, String eventType,
            String user) {
        String[] parts = objectName.split("/");

        if (parts.length <= 3) {
            return false;
        }

        Instant objectTime = Instant.parse(parts[1]);
        boolean isWithinTimeFrame = isWithinTimeFrame(objectTime, startTime, endTime);
        boolean isEventMatch = isEventMatch(parts[2], eventType);
        boolean isUserMatch = isUserMatch(parts[3], user);

        return isWithinTimeFrame && isEventMatch && isUserMatch;
    }

    private boolean isWithinTimeFrame(Instant objectTime, Instant startTime, Instant endTime) {
        return (startTime == null || !objectTime.isBefore(startTime))
                && (endTime == null || !objectTime.isAfter(endTime));
    }

    private boolean isEventMatch(String objectEvent, String eventType) {
        return eventType == null || eventType.isEmpty() || eventType.equals(objectEvent);
    }

    private boolean isUserMatch(String objectUser, String user) {
        return user == null || user.isEmpty() || user.equals(objectUser);
    }

    private String decryptMessage(String encryptedMessage) {
        try {
            return EncryptionUtil.decrypt(encryptedMessage, keyManager.getSecretKey());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
