package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.Recipe;

import java.time.OffsetDateTime;
import java.util.List;

public record RecipeSummaryResponse(
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
        OffsetDateTime createdAt
) {
    public static RecipeSummaryResponse from(Recipe r, List<String> moods) {
        return new RecipeSummaryResponse(
                r.getId(), r.getName(), r.getOwnerUserId(), r.getAuthorLabel(),
                r.getCuisine(), r.getPrepTimeMin(), r.getServings(),
                r.getIsCommunity(), r.getIsShared(), moods, r.getCreatedAt());
    }
}