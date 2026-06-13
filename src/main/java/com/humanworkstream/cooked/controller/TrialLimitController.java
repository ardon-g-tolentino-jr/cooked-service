package com.humanworkstream.cooked.controller;

import com.humanworkstream.cooked.dto.TrialLimitResponse;
import com.humanworkstream.cooked.dto.TrialLimitUpdateRequest;
import com.humanworkstream.cooked.security.SecurityUtils;
import com.humanworkstream.cooked.service.TrialLimitService;
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
public class TrialLimitController {

    private final TrialLimitService trialLimitService;
    private final SecurityUtils securityUtils;

    /** Any authenticated user — the frontend needs the config to render locked UX. */
    @GetMapping("/trial-limits")
    public ResponseEntity<List<TrialLimitResponse>> list() {
        return ResponseEntity.ok(trialLimitService.getAll().stream()
                .map(TrialLimitResponse::from)
                .toList());
    }

    /** Admin only — configure a component's access toggle / cap. */
    @PutMapping("/admin/trial-limits/{component}")
    public ResponseEntity<TrialLimitResponse> update(@PathVariable String component,
                                                     @RequestBody TrialLimitUpdateRequest req) {
        if (!securityUtils.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
        return ResponseEntity.ok(TrialLimitResponse.from(
                trialLimitService.update(component, req.accessEnabled(), req.maxCount())));
    }
}
