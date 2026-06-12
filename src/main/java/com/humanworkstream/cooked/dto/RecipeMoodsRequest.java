package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecipeMoodsRequest(
        @NotNull List<String> moods
) {
}