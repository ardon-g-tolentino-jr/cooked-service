package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.AppUser;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String email,
        String displayName,
        String handle,
        OffsetDateTime createdAt
) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getDisplayName(), u.getHandle(), u.getCreatedAt());
    }
}