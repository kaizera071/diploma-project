package com.audit.system.ingestion.schema;

import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaValidatorService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaValidatorService.class);

    private Schema schema;

    @PostConstruct
    public void init() {
        try (InputStream inputStream = getClass()
                .getResourceAsStream("/schema.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Schema file not found");
            }
            JSONObject jsonSchema = new JSONObject(new JSONTokener(inputStream));
            logger.info(jsonSchema.toString());
            this.schema = SchemaLoader.load(jsonSchema);
            logger.info("JSON schema loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load JSON schema", e);
            throw new RuntimeException("Failed to load JSON schema", e);
        }
    }

    public void validate(JSONObject json) {
        this.schema.validate(json); // throws a ValidationException if this object is invalid
    }
}