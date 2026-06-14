package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.TrialLimit;
import com.humanworkstream.cooked.repository.TrialLimitRepository;
import com.humanworkstream.cooked.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Enforces and manages the admin-configurable TRIAL access limits. Limits apply only to
 * users whose JWT carries {@code trial=true}; everyone else is unaffected.
 */
@Service
@RequiredArgsConstructor
public class TrialLimitService {

    // Component keys — mirror the seeded rows in db/feat-trial-limits/01_trial_access.sql
    public static final String MEAL_PLAN   = "meal_plan";
    public static final String RECIPES     = "recipes";
    public static final String PANTRY      = "pantry";
    public static final String SHOPPING    = "shopping";
    public static final String HISTORY     = "history";
    public static final String INGREDIENTS = "ingredients";

    private final TrialLimitRepository trialLimitRepository;
    private final SecurityUtils securityUtils;

    /**
     * Whether trial limits currently apply to the caller. True only for trial users whose
     * full-access window has ended (now >= trialUntil). During the window — or when the
     * window is unknown is treated conservatively as ended — see below. Non-trial users
     * are never limited.
     */
    private boolean isLimitedTrial() {
        if (!securityUtils.isTrial()) return false;
        Long until = securityUtils.getTrialUntil();
        // null (legacy token without the claim) → treat as limited, never grant endless full access
        return until == null || System.currentTimeMillis() >= until;
    }

    @Transactional(readOnly = true)
    public List<TrialLimit> getAll() {
        return trialLimitRepository.findAll();
    }

    @Transactional
    public TrialLimit update(String component, Boolean accessEnabled, Integer maxCount) {
        TrialLimit limit = trialLimitRepository.findById(component)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown component: " + component));
        if (accessEnabled != null) limit.setAccessEnabled(accessEnabled);
        limit.setMaxCount(maxCount);   // null clears the cap (unlimited)
        return trialLimitRepository.save(limit);
    }

    /** Block a trial user from a component whose access is disabled. No-op for non-trial users. */
    @Transactional(readOnly = true)
    public void assertEnabled(String component) {
        if (!isLimitedTrial()) return;
        trialLimitRepository.findById(component).ifPresent(limit -> {
            if (!limit.isAccessEnabled()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your trial plan doesn't include this feature. Upgrade to unlock it.");
            }
        });
    }

    /** Block a trial user at/over the component's item cap. No-op for non-trial users or no cap. */
    @Transactional(readOnly = true)
    public void assertUnderLimit(String component, long currentCount) {
        if (!isLimitedTrial()) return;
        trialLimitRepository.findById(component).ifPresent(limit -> {
            Integer max = limit.getMaxCount();
            if (max != null && currentCount >= max) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Trial limit reached: your plan allows at most " + max + ". Upgrade for more.");
            }
        });
    }
}
