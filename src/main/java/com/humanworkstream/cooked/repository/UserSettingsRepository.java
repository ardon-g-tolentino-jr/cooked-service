package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
}