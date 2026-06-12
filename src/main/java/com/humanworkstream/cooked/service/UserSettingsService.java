package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.UserSettingsPatchRequest;
import com.humanworkstream.cooked.dto.UserSettingsResponse;
import com.humanworkstream.cooked.entity.UserSettings;
import com.humanworkstream.cooked.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    @Transactional(readOnly = true)
    public UserSettingsResponse get(Long userId) {
        return UserSettingsResponse.from(getOrCreate(userId));
    }

    @Transactional
    public UserSettingsResponse patch(Long userId, UserSettingsPatchRequest req) {
        UserSettings s = getOrCreate(userId);
        if (req.accent() != null) s.setAccent(req.accent());
        if (req.kcalMode() != null) s.setKcalMode(req.kcalMode());
        if (req.readyOnly() != null) s.setReadyOnly(req.readyOnly());
        if (req.kcalGoal() != null) s.setKcalGoal(req.kcalGoal());
        return UserSettingsResponse.from(userSettingsRepository.save(s));
    }

    private UserSettings getOrCreate(Long userId) {
        return userSettingsRepository.findById(userId).orElseGet(() -> {
            log.info("[UserSettingsService] Creating default settings for userId={}", userId);
            UserSettings defaults = new UserSettings(userId, "#FF6B35", "per serving", false, 2000);
            return userSettingsRepository.save(defaults);
        });
    }
}