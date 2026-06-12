package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    // PK is the user id (one row per user)
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String accent;

    // 'per serving' or 'whole recipe'
    @Column(name = "kcal_mode", nullable = false)
    private String kcalMode;

    // Show only recipes where all ingredients are in pantry
    @Column(name = "ready_only", nullable = false)
    private Boolean readyOnly;

    @Column(name = "kcal_goal", nullable = false)
    private Integer kcalGoal;
}