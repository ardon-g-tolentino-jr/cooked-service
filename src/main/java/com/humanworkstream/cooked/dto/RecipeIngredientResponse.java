package com.humanworkstream.cooked.dto;

import java.math.BigDecimal;

public record RecipeIngredientResponse(
        Long ingredientId,
        String ingredientName,
        String category,
        BigDecimal grams,
        BigDecimal kcalPerGram
) {
}