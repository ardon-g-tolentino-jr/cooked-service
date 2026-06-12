package com.humanworkstream.cooked.dto;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String displayName
) {
}