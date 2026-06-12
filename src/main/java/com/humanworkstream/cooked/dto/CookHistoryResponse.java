package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.CookHistory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CookHistoryResponse(
        Long id,
        Long recipeId,
        String recipeName,
        BigDecimal scale,
        BigDecimal servings,
        BigDecimal totalKcal,
        OffsetDateTime cookedAt,
        List<CookHistoryItemResponse> items
) {
    public static CookHistoryResponse from(CookHistory h, List<CookHistoryItemResponse> items) {
        return new CookHistoryResponse(h.getId(), h.getRecipeId(), h.getRecipeName(),
                h.getScale(), h.getServings(), h.getTotalKcal(), h.getCookedAt(), items);
    }
}