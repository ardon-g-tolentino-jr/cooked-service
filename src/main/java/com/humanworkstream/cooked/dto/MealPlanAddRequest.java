package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.enumeration.MealSlot;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MealPlanAddRequest(
        @NotNull LocalDate planDate,
        @NotNull MealSlot mealSlot,
        @NotNull Long recipeId
) {
}
