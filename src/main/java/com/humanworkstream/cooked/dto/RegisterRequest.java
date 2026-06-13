package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String displayName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        // Optional at the DTO level so the gate can be disabled in local dev; the
        // SubscriptionGateService enforces presence when the gate is enabled.
        String registrationCode
) {
}