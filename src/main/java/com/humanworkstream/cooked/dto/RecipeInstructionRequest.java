package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecipeInstructionRequest(
        @NotNull @Min(1) Short stepNo,
        @NotBlank String instruction
) {
}