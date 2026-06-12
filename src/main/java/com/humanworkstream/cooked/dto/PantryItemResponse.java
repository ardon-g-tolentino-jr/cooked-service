package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.PantryItem;
import com.humanworkstream.cooked.enumeration.UnitType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PantryItemResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        BigDecimal qty,
        UnitType unit,
        LocalDate expiresOn,
        OffsetDateTime createdAt
) {
    public static PantryItemResponse from(PantryItem p, String ingredientName) {
        return new PantryItemResponse(p.getId(), p.getIngredientId(), ingredientName,
                p.getQty(), p.getUnit(), p.getExpiresOn(), p.getCreatedAt());
    }
}