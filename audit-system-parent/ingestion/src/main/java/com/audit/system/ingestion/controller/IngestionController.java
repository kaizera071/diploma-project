package com.audit.system.ingestion.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IngestionController {

    private static final String CONTENT_TYPE = "application/json";
    private static final String INGESTION_PATH = "/ingestion";

    @RequestMapping(method = { RequestMethod.GET }, value = INGESTION_PATH, produces = { CONTENT_TYPE }, consumes = {
            CONTENT_TYPE })
    public ResponseEntity<String> printHelloWorld() {
        return new ResponseEntity<String>("{\"Text\": \"Hello World\"}", HttpStatus.OK);
    }
}
