package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.PantryTemplate;

import java.util.List;

public record PantryTemplateResponse(
        Long id,
        String name,
        List<PantryTemplateItemResponse> items
) {
    public static PantryTemplateResponse from(PantryTemplate t, List<PantryTemplateItemResponse> items) {
        return new PantryTemplateResponse(t.getId(), t.getName(), items);
    }
}
