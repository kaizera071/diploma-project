package com.audit.system.ingestion.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {
    @KafkaListener(topics = "ingestion-topic", groupId = "consumer-group")
    public void listen(String message) {
        System.out.println("Received Message: " + message);
    }
}
