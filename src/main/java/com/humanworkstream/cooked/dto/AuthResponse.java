package com.humanworkstream.cooked.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String displayName,
        String role,
        boolean trial,
        // ISO-8601 instant until which a trial user has full access (null if not trial)
        String trialFullAccessUntil,
        boolean mustChangePassword
) {
}