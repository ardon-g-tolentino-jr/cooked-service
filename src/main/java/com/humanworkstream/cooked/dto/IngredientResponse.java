package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.Ingredient;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record IngredientResponse(
        Long id,
        String name,
        String category,
        BigDecimal kcalPerGram,
        BigDecimal gramsPerPiece,
        Boolean isBuiltin,
        Long createdBy,
        OffsetDateTime createdAt,
        // Non-null when the caller has a user override
        BigDecimal effectiveKcalPerGram,
        BigDecimal effectiveGramsPerPiece
) {
    public static IngredientResponse from(Ingredient i) {
        return new IngredientResponse(i.getId(), i.getName(), i.getCategory(),
                i.getKcalPerGram(), i.getGramsPerPiece(),
                i.getIsBuiltin(), i.getCreatedBy(), i.getCreatedAt(),
                i.getKcalPerGram(), i.getGramsPerPiece());
    }

    public static IngredientResponse from(Ingredient i, BigDecimal effectiveKcal, BigDecimal effectivePiece) {
        return new IngredientResponse(i.getId(), i.getName(), i.getCategory(),
                i.getKcalPerGram(), i.getGramsPerPiece(),
                i.getIsBuiltin(), i.getCreatedBy(), i.getCreatedAt(),
                effectiveKcal, effectivePiece);
    }
}
