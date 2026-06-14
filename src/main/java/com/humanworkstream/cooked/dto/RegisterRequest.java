package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String displayName,
        @NotBlank @Email String email,
        // No password: registration issues a system-generated temporary password by email,
        // which the user replaces on first sign-in (see AppUserService#register).
        // Optional at the DTO level so the gate can be disabled in local dev; the
        // SubscriptionGateService enforces presence when the gate is enabled.
        String registrationCode
) {
}
