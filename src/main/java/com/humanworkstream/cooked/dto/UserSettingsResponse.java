package com.humanworkstream.cooked.dto;

import com.humanworkstream.cooked.entity.UserSettings;

public record UserSettingsResponse(
        String accent,
        String kcalMode,
        Boolean readyOnly,
        Integer kcalGoal
) {
    public static UserSettingsResponse from(UserSettings s) {
        return new UserSettingsResponse(s.getAccent(), s.getKcalMode(), s.getReadyOnly(), s.getKcalGoal());
    }
}