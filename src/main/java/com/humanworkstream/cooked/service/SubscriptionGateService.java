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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    /** Deny login unless the email has an active access record for this service. Fail-closed.
     * @return the trial flag plus the resolved tier (the subscription plan backing the access). */
    public GateResult assertActiveAccess(String email) {
        if (!enabled) {
            log.warn("[SubscriptionGate] disabled — skipping access check for {}", email);
            return new GateResult(false, null);
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
        List<String> planNames = cookedAccess.stream()
                .map(AccessRecord::planName)
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .toList();
        return new GateResult(trial, resolveTier(planNames));
    }

    /** Outcome of an access check: whether it's a trial code, and the resolved tier (plan name). */
    public record GateResult(boolean trial, String tier) {}

    /**
     * Resolve the user's tier from their active plan name(s). The tier list is owned by the
     * subscription service — when a user has more than one active plan, the highest-priced
     * (by the subscription catalog amount) wins. Single/zero plans need no catalog lookup, so
     * the common path makes no extra call. Fail-soft: a catalog error falls back to the first
     * plan name rather than denying access (the access check already passed).
     */
    private String resolveTier(List<String> planNames) {
        if (planNames.isEmpty()) return null;
        if (planNames.size() == 1) return planNames.get(0);
        Map<String, BigDecimal> amounts = tierCatalogSafe().stream()
                .collect(Collectors.toMap(t -> t.name().toLowerCase(Locale.ROOT),
                        TierInfo::amount, (a, b) -> a));
        return planNames.stream()
                .max(Comparator.comparing(n -> amounts.getOrDefault(n.toLowerCase(Locale.ROOT), BigDecimal.ZERO)))
                .orElse(planNames.get(0));
    }

    /** Active COOKED tier names, highest-priced first — for the admin matrix. Empty on error. */
    public List<String> listTierNames() {
        return tierCatalogSafe().stream()
                .sorted(Comparator.comparing(TierInfo::amount).reversed())
                .map(TierInfo::name)
                .toList();
    }

    /** Fetch the COOKED plans (tiers) from the subscription service. Never throws. */
    private List<TierInfo> tierCatalogSafe() {
        if (!enabled) return List.of();
        try {
            List<PlanRecord> plans = restClient.get()
                    .uri(b -> b.path("/api/plans").queryParam("serviceCode", serviceCode).build())
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PlanRecord>>() {});
            if (plans == null) return List.of();
            return plans.stream()
                    .filter(p -> p.name() != null && !Boolean.FALSE.equals(p.isActive()))
                    .map(p -> new TierInfo(p.name(), p.amount() != null ? p.amount() : BigDecimal.ZERO))
                    .toList();
        } catch (Exception e) {
            log.warn("[SubscriptionGate] could not fetch {} tier catalog ({})", serviceCode, e.toString());
            return List.of();
        }
    }

    /** A COOKED tier as defined by the subscription service. */
    public record TierInfo(String name, BigDecimal amount) {}

    /** Subset of the subscription PlanResponse we care about. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlanRecord(
            @JsonProperty("name") String name,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("isActive") Boolean isActive) {
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
            @JsonProperty("regCode") String regCode,
            @JsonProperty("planId") Integer planId,
            @JsonProperty("planName") String planName) {
    }
}
