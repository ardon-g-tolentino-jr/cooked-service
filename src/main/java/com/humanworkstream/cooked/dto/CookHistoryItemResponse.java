package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.CookHistoryItem;

import java.math.BigDecimal;

public record CookHistoryItemResponse(
        String ingredientName,
        BigDecimal grams,
        BigDecimal kcal
) {
    public static CookHistoryItemResponse from(CookHistoryItem i) {
        return new CookHistoryItemResponse(i.getIngredientName(), i.getGrams(), i.getKcal());
    }
}