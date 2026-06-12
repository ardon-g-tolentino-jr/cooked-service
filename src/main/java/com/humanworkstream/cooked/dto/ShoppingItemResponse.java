package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.ShoppingItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ShoppingItemResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        BigDecimal grams,
        OffsetDateTime createdAt
) {
    public static ShoppingItemResponse from(ShoppingItem s, String ingredientName) {
        return new ShoppingItemResponse(s.getId(), s.getIngredientId(), ingredientName,
                s.getGrams(), s.getCreatedAt());
    }
}