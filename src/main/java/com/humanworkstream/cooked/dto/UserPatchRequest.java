package com.humanworkstream.cooked.dto;

public record UserPatchRequest(
        String displayName,
        String handle
) {
}
