package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CookRequest(
        Long recipeId,
        @NotBlank String recipeName,
        @NotNull @DecimalMin("0.01") BigDecimal scale,
        @NotNull @Min(1) Integer servings,
        @NotEmpty List<IngredientEntry> ingredients
) {
    public record IngredientEntry(
            @NotNull Long ingredientId,
            @NotNull @DecimalMin("0.01") BigDecimal grams
    ) {}
}