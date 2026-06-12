package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.Recipe;

import java.time.OffsetDateTime;
import java.util.List;

public record RecipeDetailResponse(
        Long id,
        String name,
        Long ownerUserId,
        String authorLabel,
        String cuisine,
        Integer prepTimeMin,
        Integer servings,
        Boolean isCommunity,
        Boolean isShared,
        List<String> moods,
        List<RecipeIngredientResponse> ingredients,
        List<RecipeInstructionResponse> instructions,
        OffsetDateTime createdAt
) {
    public static RecipeDetailResponse from(Recipe r,
                                            List<String> moods,
                                            List<RecipeIngredientResponse> ingredients,
                                            List<RecipeInstructionResponse> instructions) {
        return new RecipeDetailResponse(
                r.getId(), r.getName(), r.getOwnerUserId(), r.getAuthorLabel(),
                r.getCuisine(), r.getPrepTimeMin(), r.getServings(),
                r.getIsCommunity(), r.getIsShared(), moods, ingredients, instructions,
                r.getCreatedAt());
    }
}