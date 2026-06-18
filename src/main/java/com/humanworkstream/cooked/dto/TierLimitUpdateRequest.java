package com.humanworkstream.cooked.dto;

/** Admin update for a tier limit. Both fields optional; maxCount=null clears the cap. */
public record TierLimitUpdateRequest(
        Boolean accessEnabled,
        Integer maxCount
) {
}
