package com.humanworkstream.cooked.dto;

public record UserSettingsPatchRequest(
        String accent,
        String kcalMode,
        Boolean readyOnly,
        Integer kcalGoal
) {
}