package com.audit.system.ingestion.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.audit.system.ingestion.minio.MinioService;

@Service
public class KafkaConsumerService {

    @Autowired
    private MinioService minioService;

    @KafkaListener(topics = "ingestion-topic", groupId = "consumer-group")
    public void listen(String message) {
        minioService.saveToMinIO(message);
    }
}
