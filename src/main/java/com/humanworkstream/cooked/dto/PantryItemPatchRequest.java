package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.enumeration.UnitType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PantryItemPatchRequest(
        BigDecimal qty,
        UnitType unit,
        LocalDate expiresOn
) {
}