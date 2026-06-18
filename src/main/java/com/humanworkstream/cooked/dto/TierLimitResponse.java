package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.TierLimit;

public record TierLimitResponse(
        String tier,
        String component,
        boolean accessEnabled,
        Integer maxCount
) {
    public static TierLimitResponse from(TierLimit t) {
        return new TierLimitResponse(
                t.getId().getTier(), t.getId().getComponent(), t.isAccessEnabled(), t.getMaxCount());
    }
}
