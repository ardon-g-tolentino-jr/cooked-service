package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.Tier;
import com.humanworkstream.cooked.entity.TierLimit;
import com.humanworkstream.cooked.repository.TierLimitRepository;
import com.humanworkstream.cooked.repository.TierRepository;
import com.humanworkstream.cooked.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Per-tier (subscription plan) access control. Independent of {@link TrialLimitService}:
 * both axes are enforced at each seam and either can block. A user's tier is resolved at
 * login from their subscription plan(s) and carried in the JWT; limits apply to everyone
 * whose tier has a matching {@code tier_limit} row. Users with no resolved tier are
 * unrestricted by this axis. Reuses the component vocabulary from {@link TrialLimitService}.
 */
@Service
@RequiredArgsConstructor
public class TierLimitService {

    private final TierRepository tierRepository;
    private final TierLimitRepository tierLimitRepository;
    private final SecurityUtils securityUtils;

    /**
     * Resolve the winning tier among the given plan names: the known {@code tier} with the
     * highest rank. Case-insensitive match against the registry. Null when none are known.
     */
    @Transactional(readOnly = true)
    public String resolveTier(Collection<String> planNames) {
        if (planNames == null || planNames.isEmpty()) return null;
        Set<String> wanted = planNames.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return tierRepository.findAll().stream()
                .filter(t -> wanted.contains(t.getName().toLowerCase(Locale.ROOT)))
                .max(Comparator.comparingInt(Tier::getRank))
                .map(Tier::getName)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<TierLimit> getAll() {
        return tierLimitRepository.findAll();
    }

    @Transactional
    public TierLimit update(String tier, String component, Boolean accessEnabled, Integer maxCount) {
        TierLimit limit = tierLimitRepository.findByIdTierAndIdComponent(tier, component)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Unknown tier/component: " + tier + "/" + component));
        if (accessEnabled != null) limit.setAccessEnabled(accessEnabled);
        limit.setMaxCount(maxCount);   // null clears the cap (unlimited)
        return tierLimitRepository.save(limit);
    }

    /** Block a user whose tier disables this component. No-op when the user has no tier. */
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
