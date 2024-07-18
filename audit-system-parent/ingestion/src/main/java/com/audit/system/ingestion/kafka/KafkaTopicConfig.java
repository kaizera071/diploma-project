package com.audit.system.ingestion.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic topicExample() {
        return TopicBuilder.name("ingestion-topic")
                .partitions(8)
                .replicas(3)
                .build();
    }
}
