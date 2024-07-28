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
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            // Assuming the data is encrypted, we decrypt it here
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
        // Adjust prefix handling for wildcards
        String prefix = "";
        if (tenant != null && !tenant.isEmpty()) {
            prefix += tenant + "/";
        }

        List<String> objectNames = listObjects(bucketName, prefix);

        // Filter objects based on time frame and other optional parameters
        return objectNames.stream()
                .filter(objectName -> {
                    String[] parts = objectName.split("/");
                    if (parts.length > 3) {
                        Instant objectTime = Instant.parse(parts[1]);
                        boolean timeMatch = (startTime == null || !objectTime.isBefore(startTime))
                                && (endTime == null || !objectTime.isAfter(endTime));
                        boolean eventMatch = (eventType == null || eventType.isEmpty() || eventType.equals(parts[2]));
                        boolean userMatch = (user == null || user.isEmpty() || user.equals(parts[3]));
                        return timeMatch && eventMatch && userMatch;
                    }
                    return false;
                })
                .map(objectName -> readObject(bucketName, objectName))
                .collect(Collectors.toList());
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
