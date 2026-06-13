package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.MealPlanEntry;
import com.humanworkstream.cooked.enumeration.MealSlot;

import java.time.LocalDate;

public record MealPlanEntryResponse(
        Long id,
        LocalDate planDate,
        MealSlot mealSlot,
        Long recipeId,
        String recipeName
) {
    public static MealPlanEntryResponse from(MealPlanEntry m, String recipeName) {
        return new MealPlanEntryResponse(m.getId(), m.getPlanDate(), m.getMealSlot(),
                m.getRecipeId(), recipeName);
    }
}
