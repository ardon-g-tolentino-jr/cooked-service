package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.TrialLimit;

public record TrialLimitResponse(
        String component,
        boolean accessEnabled,
        Integer maxCount
) {
    public static TrialLimitResponse from(TrialLimit t) {
        return new TrialLimitResponse(t.getComponent(), t.isAccessEnabled(), t.getMaxCount());
    }
}
