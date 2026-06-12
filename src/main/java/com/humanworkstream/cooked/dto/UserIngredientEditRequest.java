package com.humanworkstream.cooked.dto;

import java.math.BigDecimal;

public record UserIngredientEditRequest(
        BigDecimal kcalPerGram,
        BigDecimal gramsPerPiece
) {
}