package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecipeIngredientRequest(
        @NotNull Long ingredientId,
        @NotNull @DecimalMin("0.01") BigDecimal grams
) {
}