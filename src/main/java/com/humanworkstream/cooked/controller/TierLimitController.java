package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.TierLimitResponse;
import com.humanworkstream.cooked.dto.TierLimitUpdateRequest;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.SubscriptionGateService;
import com.humanworkstream.cooked.service.TierLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TierLimitController {

    private final TierLimitService tierLimitService;
    private final SubscriptionGateService subscriptionGate;
    private final SecurityUtils securityUtils;

    /** The available tiers, sourced from the subscription service (highest-priced first). */
    @GetMapping("/tiers")
    public ResponseEntity<List<String>> tiers() {
        return ResponseEntity.ok(subscriptionGate.listTierNames());
    }

    /** Any authenticated user — the frontend needs the matrix to render tier-gated UX. */
    @GetMapping("/tier-limits")
    public ResponseEntity<List<TierLimitResponse>> list() {
        return ResponseEntity.ok(tierLimitService.getAll().stream()
                .map(TierLimitResponse::from)
                .toList());
    }

    /** Admin only — configure a (tier, component)'s access toggle / cap. */
    @PutMapping("/admin/tier-limits/{tier}/{component}")
    public ResponseEntity<TierLimitResponse> update(@PathVariable String tier,
                                                    @PathVariable String component,
                                                    @RequestBody TierLimitUpdateRequest req) {
        if (!securityUtils.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
        return ResponseEntity.ok(TierLimitResponse.from(
                tierLimitService.update(tier, component, req.accessEnabled(), req.maxCount())));
    }
}
