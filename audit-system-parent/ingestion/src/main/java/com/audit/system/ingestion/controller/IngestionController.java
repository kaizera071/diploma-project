package com.audit.system.ingestion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audit.system.ingestion.kafka.KafkaProducerService;

@RestController
@RequestMapping("/ingestion")
public class IngestionController {

    private final KafkaProducerService producerService;

    @Autowired
    public IngestionController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    private static final String CONTENT_TYPE = "application/json";

    @RequestMapping(method = { RequestMethod.POST }, value = "/send", produces = { CONTENT_TYPE }, consumes = {
            CONTENT_TYPE })
    public void sendMessage(@RequestParam String topic, @RequestBody Object message) {
        producerService.sendMessage(topic, message);
    }

}
