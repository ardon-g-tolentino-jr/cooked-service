package com.humanworkstream.cooked.dto;

/** Admin update for a trial limit. Both fields optional; maxCount=null clears the cap. */
public record TrialLimitUpdateRequest(
        Boolean accessEnabled,
        Integer maxCount
) {
}
