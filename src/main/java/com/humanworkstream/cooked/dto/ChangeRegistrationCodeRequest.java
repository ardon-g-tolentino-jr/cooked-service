package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.NotBlank;

/** Authenticated in-app registration-code change: apply a new code to the signed-in user. */
public record ChangeRegistrationCodeRequest(
        @NotBlank String registrationCode
) {
}
