package com.audit.system.ingestion.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audit.system.ingestion.kafka.KafkaProducerService;
import com.audit.system.ingestion.schema.JsonSchemaValidatorService;

@RestController
@RequestMapping("/ingestion")
public class IngestionController {

    private final KafkaProducerService producerService;

    @Autowired
    private JsonSchemaValidatorService jsonSchemaValidatorService;

    @Autowired
    public IngestionController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    private static final String CONTENT_TYPE = "application/json";

    @RequestMapping(method = RequestMethod.POST, value = "/send", produces = { CONTENT_TYPE }, consumes = {
            CONTENT_TYPE })
    public ResponseEntity<String> sendMessage(@RequestParam String topic, @RequestBody String message) {
        try {
            // Parse and validate the message
            JSONObject jsonMessage = new JSONObject(message);
            jsonSchemaValidatorService.validate(jsonMessage);

            // If valid, send the message
            producerService.sendMessage(topic, message);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            // Return a bad request response if validation fails
            return ResponseEntity.badRequest().body("JSON validation error: " + e.getMessage());
        }
    }

}
