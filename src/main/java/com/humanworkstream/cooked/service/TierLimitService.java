package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.TierLimit;
import com.humanworkstream.cooked.entity.id.TierLimitId;
import com.humanworkstream.cooked.repository.TierLimitRepository;
import com.humanworkstream.cooked.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Per-tier feature settings. The tier list is owned by the subscription service (a tier = a
 * COOKED plan name); this service only stores, per (tier, component), whether the feature is
 * enabled and an optional item cap. Rows are sparse — a missing row means "allowed" — so the
 * admin only records the restrictions. Independent of {@link TrialLimitService}: both axes are
 * enforced at each seam and either can block. A user's tier comes from the JWT (resolved at
 * login by the gate); users with no tier are unrestricted by this axis. Reuses the component
 * vocabulary from {@link TrialLimitService}.
 */
@Service
@RequiredArgsConstructor
public class TierLimitService {

    private final TierLimitRepository tierLimitRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<TierLimit> getAll() {
        return tierLimitRepository.findAll();
    }

    /** Upsert the setting for a (tier, component). Creates the row if the admin hasn't set one yet. */
    @Transactional
    public TierLimit update(String tier, String component, Boolean accessEnabled, Integer maxCount) {
        if (tier == null || tier.isBlank() || component == null || component.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tier and component are required");
        }
        TierLimit limit = tierLimitRepository.findByIdTierAndIdComponent(tier, component)
                .orElseGet(() -> {
                    TierLimit t = new TierLimit();
                    t.setId(new TierLimitId(tier, component));
                    return t;
                });
        if (accessEnabled != null) limit.setAccessEnabled(accessEnabled);
        limit.setMaxCount(maxCount);   // null clears the cap (unlimited)
        limit.setUpdatedAt(OffsetDateTime.now());
        return tierLimitRepository.save(limit);
    }

    /** Block a user whose tier disables this component. No-op when the user has no tier or no row. */
    @Transactional(readOnly = true)
    public void assertEnabled(String component) {
        String tier = securityUtils.getTier();
        if (tier == null) return;
        tierLimitRepository.findByIdTierAndIdComponent(tier, component).ifPresent(limit -> {
            if (!limit.isAccessEnabled()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your " + tier + " plan doesn't include this feature. Upgrade to unlock it.");
            }
        });
    }

    /** Block a user at/over their tier's item cap for this component. No-op when no tier or no cap. */
    @Transactional(readOnly = true)
    public void assertUnderLimit(String component, long currentCount) {
        String tier = securityUtils.getTier();
        if (tier == null) return;
        tierLimitRepository.findByIdTierAndIdComponent(tier, component).ifPresent(limit -> {
            Integer max = limit.getMaxCount();
            if (max != null && currentCount >= max) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Your " + tier + " plan allows at most " + max + ". Upgrade for more.");
            }
        });
    }
}
