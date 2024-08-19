/*
 * package com.audit.system.forwarder;
 * 
 * import static org.mockito.ArgumentMatchers.any;
 * import static org.mockito.Mockito.*;
 * 
 * import java.io.ByteArrayInputStream;
 * import java.io.InputStream;
 * import java.util.HashMap;
 * import java.util.Iterator;
 * import java.util.Map;
 * 
 * import javax.crypto.SecretKey;
 * 
 * import com.fasterxml.jackson.databind.JsonNode;
 * import com.fasterxml.jackson.databind.ObjectMapper;
 * import io.minio.MinioClient;
 * import io.minio.PutObjectArgs;
 * import org.junit.jupiter.api.Test;
 * import org.junit.jupiter.api.extension.ExtendWith;
 * import org.mockito.InjectMocks;
 * import org.mockito.Mock;
 * import org.mockito.junit.jupiter.MockitoExtension;
 * 
 * import com.audit.system.forwarder.minio.MinioService;
 * import com.audit.system.forwarder.security.EncryptionUtil;
 * import com.audit.system.forwarder.security.KeyManager;
 * 
 * @ExtendWith(MockitoExtension.class)
 * class MinioServiceTest {
 * 
 * @Mock
 * private ObjectMapper objectMapper;
 * 
 * @Mock
 * private MinioClient minioClient;
 * 
 * @Mock
 * private KeyManager keyManager;
 * 
 * @InjectMocks
 * private MinioService minioService;
 * 
 * @Test
 * void testSaveToMinIO() throws Exception {
 * // Prepare test data
 * String tenant = "tenant1";
 * String time = "2023-01-01T00:00:00Z";
 * String eventType = "login";
 * String user = "user123";
 * String message = String.format(
 * "{\"audit_event\":{\"%s\":{\"tenant\":\"%s\",\"time\":\"%s\",\"user\":\"%s\"}}}",
 * eventType, tenant, time, user);
 * 
 * // Mock JsonNode objects
 * JsonNode mockJsonNode = mock(JsonNode.class);
 * JsonNode mockAuditEventNode = mock(JsonNode.class);
 * JsonNode mockEventDataNode = mock(JsonNode.class);
 * 
 * // Stubbing
 * when(objectMapper.readTree(message)).thenReturn(mockJsonNode);
 * when(mockJsonNode.path("audit_event")).thenReturn(mockAuditEventNode);
 * when(mockAuditEventNode.fields()).thenReturn(getMockFields(eventType,
 * mockEventDataNode));
 * when(mockEventDataNode.path("tenant")).thenReturn(mockTextNode(tenant));
 * when(mockEventDataNode.path("time")).thenReturn(mockTextNode(time));
 * when(mockEventDataNode.path("user")).thenReturn(mockTextNode(user));
 * 
 * String prettyString = "prettyJson";
 * when(objectMapper.writeValueAsString(mockJsonNode)).thenReturn(prettyString);
 * 
 * SecretKey secretKey = mock(SecretKey.class);
 * when(keyManager.getSecretKey()).thenReturn(secretKey);
 * 
 * String encryptedMessage = "encryptedMessage";
 * when(EncryptionUtil.encrypt(prettyString,
 * secretKey)).thenReturn(encryptedMessage);
 * 
 * // Run the method under test
 * minioService.saveToMinIO(message);
 * 
 * // Verify interactions
 * verify(minioClient).putObject(argThat(args -> {
 * String expectedObjectKey = String.format("%s/%s/%s/%s/message-", tenant,
 * time, eventType, user);
 * try (InputStream is = new ByteArrayInputStream(encryptedMessage.getBytes()))
 * {
 * return args.bucket().equals("test-bucket") &&
 * args.object().startsWith(expectedObjectKey) &&
 * args.contentType().equals("application/json") &&
 * args.stream().available() == is.available();
 * } catch (Exception e) {
 * return false;
 * }
 * }));
 * }
 * 
 * // Helper methods to mock JSON nodes
 * private Iterator<Map.Entry<String, JsonNode>> getMockFields(String key,
 * JsonNode valueNode) {
 * Map<String, JsonNode> map = new HashMap<>();
 * map.put(key, valueNode);
 * return map.entrySet().iterator();
 * }
 * 
 * private JsonNode mockTextNode(String text) {
 * JsonNode textNode = mock(JsonNode.class);
 * when(textNode.asText()).thenReturn(text);
 * return textNode;
 * }
 * }
 */