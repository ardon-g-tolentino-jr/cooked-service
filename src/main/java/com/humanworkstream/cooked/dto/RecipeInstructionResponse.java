package com.humanworkstream.cooked.dto;

public record RecipeInstructionResponse(
        Short stepNo,
        String instruction
) {
}