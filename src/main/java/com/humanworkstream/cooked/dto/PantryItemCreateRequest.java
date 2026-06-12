package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.enumeration.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PantryItemCreateRequest(
        @NotNull Long ingredientId,
        @NotNull @DecimalMin("0") BigDecimal qty,
        @NotNull UnitType unit,
        LocalDate expiresOn
) {
}