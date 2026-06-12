package com.humanworkstream.cooked.dto;

public record AuthResponse(
        String token,
        Integer userId,
        String email,
        String name
) {
}