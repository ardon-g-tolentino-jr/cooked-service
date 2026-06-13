package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for POST /auth/google — the Google Sign-In ID token from the browser. */
public record GoogleLoginRequest(
        @NotBlank String idToken
) {
}
