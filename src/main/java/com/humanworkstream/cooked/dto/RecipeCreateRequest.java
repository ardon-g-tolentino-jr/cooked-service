package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecipeCreateRequest(
        @NotBlank String name,
        @NotBlank String cuisine,
        @NotNull @Min(1) Integer prepTimeMin,
        @NotNull @Min(1) Integer servings,
        List<String> moods,
        List<RecipeIngredientRequest> ingredients,
        List<RecipeInstructionRequest> instructions
) {
}