package com.audit.system.retrieval.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audit.system.retrieval.minio.MinioService;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/retrieval")
public class RetrievalController {

        @Autowired
        private MinioService minioService;

        @GetMapping("/read")
        public ResponseEntity<Object[]> read(
                        @RequestParam(required = false) String tenant,
                        @RequestParam(required = false) String eventType,
                        @RequestParam(required = false) String user,
                        @RequestParam(required = false) String startTime,
                        @RequestParam(required = false) String endTime) {

                Instant startInstant = startTime != null ? Instant.parse(startTime) : null;
                Instant endInstant = endTime != null ? Instant.parse(endTime) : null;

                List<JsonNode> results = minioService.searchObjects("audit-bucket", tenant, eventType, user,
                                startInstant, endInstant);

                // Create the response body as an Object array
                Object[] responseBody = new Object[] {
                                "Successfully retrieved " + results.size() + " records.",
                                results
                };

                // Return the response with status 200 OK
                return ResponseEntity.ok(responseBody);
        }
}
