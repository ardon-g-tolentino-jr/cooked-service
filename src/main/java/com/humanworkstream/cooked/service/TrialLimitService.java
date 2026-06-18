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
     * Whether the built-in TRIAL TIER currently applies to the caller — i.e. they are a trial
     * user still within their window. The TRIAL TIER enables all features but caps the recipe
     * count. After the window ends the user degrades to the FREE subscription tier (enforced by
     * {@link TierLimitService}), so this returns false. Non-trial users are never on the trial tier.
     */
    private boolean isActiveTrial() {
        if (!securityUtils.isTrial()) return false;
        Long until = securityUtils.getTrialUntil();
        // null (legacy token without the claim) → treat as already expired (no endless trial tier)
        return until != null && System.currentTimeMillis() < until;
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

    /**
     * No-op: the TRIAL TIER enables every feature. (Feature-level restrictions only apply after
     * the trial expires, via the FREE tier in {@link TierLimitService}.) Kept so call sites can
     * pair it with the tier check uniformly.
     */
    @Transactional(readOnly = true)
    public void assertEnabled(String component) {
        // intentionally empty — all features are available during the trial
    }

    /** Cap an active trial user at the component's item limit (e.g. the trial recipe count).
     * No-op for non-trial/expired users or components without a configured cap. */
    @Transactional(readOnly = true)
    public void assertUnderLimit(String component, long currentCount) {
        if (!isActiveTrial()) return;
        trialLimitRepository.findById(component).ifPresent(limit -> {
            Integer max = limit.getMaxCount();
            if (max != null && currentCount >= max) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Trial limit reached: your trial allows at most " + max + ". Upgrade for more.");
            }
        });
    }
}
