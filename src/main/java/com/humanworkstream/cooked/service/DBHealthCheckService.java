package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.repository.DBHealthCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DBHealthCheckService {

    private final DBHealthCheckRepository dbHealthCheckRepository;

    public boolean isDatabaseHealthy() {
        try {
            Integer result = dbHealthCheckRepository.checkDB();
            boolean healthy = result != null && result == 1;
            log.info("[DBHealthCheckService] Database health check result={} healthy={}", result, healthy);
            return healthy;
        } catch (Exception e) {
            log.error("[DBHealthCheckService] Database health check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isSchemaHealthy() {
        try {
            Integer result = dbHealthCheckRepository.checkSchema();
            boolean healthy = result != null && result >= 0;
            log.info("[DBHealthCheckService] Schema health check result={} healthy={}", result, healthy);
            return healthy;
        } catch (Exception e) {
            log.error("[DBHealthCheckService] Schema health check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}