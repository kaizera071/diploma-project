package com.audit.system.retrieval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audit.system.retrieval.minio.MinioService;
import com.audit.system.retrieval.security.KeyManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KeyManager keyManager;

    @InjectMocks
    private MinioService minioServiceMock;

    @BeforeEach
    void setUp() {
        // Additional setup can be done here if needed
    }

    @Test
    void testListObjects() throws Exception {
        // Mock response from MinioClient
        Item item1 = mock(Item.class);
        Item item2 = mock(Item.class);

        when(item1.objectName()).thenReturn("object1.json");
        when(item2.objectName()).thenReturn("object2.json");

        Result<Item> result1 = mock(Result.class);
        Result<Item> result2 = mock(Result.class);

        when(result1.get()).thenReturn(item1);
        when(result2.get()).thenReturn(item2);

        Iterable<Result<Item>> results = Arrays.asList(result1, result2);

        when(minioClient.listObjects(any())).thenReturn(results);

        List<String> objects = minioServiceMock.listObjects("bucket", "prefix");
        assertEquals(2, objects.size());
        assertTrue(objects.contains("object1.json"));
        assertTrue(objects.contains("object2.json"));
    }
}