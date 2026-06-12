package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.service.DBHealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final DBHealthCheckService dbHealthService;

    @GetMapping("/healthcheck")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/db/healthcheck")
    public ResponseEntity<Map<String, String>> dbHealth() {
        boolean isHealthy = dbHealthService.isDatabaseHealthy();

        if (isHealthy) {
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "database", "PostgreSQL is reachable"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "DOWN",
                            "database", "PostgreSQL connection failed"
                    ));
        }
    }

    @GetMapping("/db/schema/healthcheck")
    public ResponseEntity<Map<String, String>> dbSchemaHealth() {
        boolean isHealthy = dbHealthService.isSchemaHealthy();

        if (isHealthy) {
            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "schema", "Database schema is healthy"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "DOWN",
                            "schema", "Database schema check failed"
                    ));
        }
    }
}