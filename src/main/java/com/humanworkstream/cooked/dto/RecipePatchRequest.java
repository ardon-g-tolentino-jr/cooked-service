package com.humanworkstream.cooked.dto;

public record RecipePatchRequest(
        String name,
        String cuisine,
        Integer prepTimeMin,
        Integer servings,
        Boolean isShared
) {
}