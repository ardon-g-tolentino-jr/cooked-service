package com.humanworkstream.cooked.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Gates Cooked access against the Subscription service. All calls are server-to-server,
 * authenticated with an {@code X-Api-Key} header that never reaches the browser.
 *
 * <ul>
 *   <li>{@link #provisionViaCode} — at signup, validate + redeem a registration code,
 *       which provisions {@code COOKED} access (CODE or SUBSCRIPTION) on the subscription side.</li>
 *   <li>{@link #assertActiveAccess} — at login, allow in only if the email has an active
 *       access record for this service. Fail-closed: any error denies access.</li>
 * </ul>
 */
@Slf4j
@Service
public class SubscriptionGateService {

    private final RestClient restClient;
    private final String apiKey;
    private final String serviceCode;
    private final boolean enabled;

    public SubscriptionGateService(
            @Value("${subscription.base-url}") String baseUrl,
            @Value("${subscription.api-key}") String apiKey,
            @Value("${subscription.service-code}") String serviceCode,
            @Value("${subscription.gate.enabled}") boolean enabled) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.serviceCode = serviceCode;
        this.enabled = enabled;
        log.info("[SubscriptionGate] enabled={} serviceCode={} baseUrl={}", enabled, serviceCode, baseUrl);
    }

    /**
     * Validate and redeem a registration code, creating/reusing the subscription-side client
     * and granting {@code serviceCode} access. Throws if the code is missing or rejected.
     */
    public void provisionViaCode(String displayName, String email, String password, String code) {
        if (!enabled) {
            log.warn("[SubscriptionGate] disabled — skipping code redemption for {}", email);
            return;
        }
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A registration code is required");
        }
        // The subscription /code endpoint upserts the client by email and redeems the code in one call.
        Map<String, String> body = Map.of(
                "name", displayName,
                "doctorName", displayName,
                "email", email,
                "password", password,
                "registrationCode", code);
        try {
            restClient.post()
                    .uri("/api/subscription/code")
                    .header("X-Api-Key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("[SubscriptionGate] redeemed code for {}", email);
        } catch (RestClientResponseException e) {
            // Safety net for a partial prior signup: if the email already has active access,
            // the subscription side answers 409 (DuplicateAccess) and does NOT consume the code.
            // Treat that as success so registration is idempotent and can finish creating the
            // local account that the earlier attempt failed to persist.
            if (e.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
                log.info("[SubscriptionGate] {} already has active access — treating redemption as a no-op (idempotent)", email);
                return;
            }
            String msg = extractMessage(e, "Your registration code could not be redeemed.");
            log.warn("[SubscriptionGate] code redemption rejected for {}: {} {}", email, e.getStatusCode(), msg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        } catch (Exception e) {
            log.error("[SubscriptionGate] subscription service unreachable during signup for {}", email, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to verify your registration code right now. Please try again later.");
        }
    }

    /** Deny login unless the email has an active access record for this service. Fail-closed. */
    /** @return true if the matched active COOKED access came from a TRIAL registration code. */
    public boolean assertActiveAccess(String email) {
        if (!enabled) {
            log.warn("[SubscriptionGate] disabled — skipping access check for {}", email);
            return false;
        }
        List<AccessRecord> records;
        try {
            records = restClient.get()
                    .uri(b -> b.path("/api/clients/access/by-email").queryParam("email", email).build())
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AccessRecord>>() {});
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                // No subscription client / no access for this email.
                throw noAccess();
            }
            log.error("[SubscriptionGate] access check failed for {}: {}", email, e.getStatusCode());
            throw unavailable();
        } catch (Exception e) {
            log.error("[SubscriptionGate] subscription service unreachable during login for {}", email, e);
            throw unavailable();
        }
        List<AccessRecord> cookedAccess = records == null ? List.of() : records.stream()
                .filter(r -> Boolean.TRUE.equals(r.isActive()) && serviceCode.equalsIgnoreCase(r.serviceCode()))
                .toList();
        if (cookedAccess.isEmpty()) {
            log.warn("[SubscriptionGate] no active {} access for {}", serviceCode, email);
            throw noAccess();
        }
        boolean trial = cookedAccess.stream()
                .anyMatch(r -> r.regCode() != null && r.regCode().toUpperCase().contains("TRIAL"));
        if (trial) log.info("[SubscriptionGate] {} is on a TRIAL access code", email);
        return trial;
    }

    /**
     * Non-throwing check: does this email already have active access for this service?
     * Used at signup to reconcile a partial prior registration — if access already exists
     * we skip code redemption and just create the local account. Any error (including the
     * gate being disabled or the subscription service being unreachable) returns false so
     * the caller falls back to normal code redemption, which has its own error handling.
     */
    public boolean hasActiveAccess(String email) {
        if (!enabled) {
            return false;
        }
        try {
            List<AccessRecord> records = restClient.get()
                    .uri(b -> b.path("/api/clients/access/by-email").queryParam("email", email).build())
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AccessRecord>>() {});
            return records != null && records.stream()
                    .anyMatch(r -> Boolean.TRUE.equals(r.isActive()) && serviceCode.equalsIgnoreCase(r.serviceCode()));
        } catch (Exception e) {
            log.warn("[SubscriptionGate] active-access pre-check failed for {} ({}) — will attempt code redemption", email, e.toString());
            return false;
        }
    }

    private ResponseStatusException noAccess() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN,
                "No active Cooked access for this account. Register with a valid code or subscribe to continue.");
    }

    private ResponseStatusException unavailable() {
        // Fail-closed: an unverifiable access state must not grant entry.
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to verify your Cooked access right now. Please try again later.");
    }

    private String extractMessage(RestClientResponseException e, String fallback) {
        try {
            Map<?, ?> body = e.getResponseBodyAs(Map.class);
            if (body != null && body.get("message") instanceof String m && !m.isBlank()) {
                return m;
            }
        } catch (Exception ignored) {
            // non-JSON error body — fall through
        }
        return fallback;
    }

    /** Subset of the subscription ClientAccessResponse we care about. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccessRecord(
            @JsonProperty("serviceCode") String serviceCode,
            @JsonProperty("isActive") Boolean isActive,
            @JsonProperty("regCode") String regCode) {
    }
}
