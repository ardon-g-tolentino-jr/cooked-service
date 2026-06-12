package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IngredientCreateRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotNull @DecimalMin("0") BigDecimal kcalPerGram,
        BigDecimal gramsPerPiece
) {
}