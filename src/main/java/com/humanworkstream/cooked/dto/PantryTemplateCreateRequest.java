package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PantryTemplateCreateRequest(
        @NotBlank String name,
        @NotEmpty List<Long> ingredientIds
) {
}
