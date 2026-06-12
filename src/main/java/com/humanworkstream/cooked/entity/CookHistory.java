package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cook_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // NULL if cooked ad-hoc without a saved recipe
    @Column(name = "recipe_id")
    private Long recipeId;

    // Snapshot of the recipe name at cook time
    @Column(name = "recipe_name", nullable = false)
    private String recipeName;

    // Multiplier applied to base recipe quantities
    @Column(nullable = false)
    private BigDecimal scale;

    @Column(nullable = false)
    private BigDecimal servings;

    @Column(name = "total_kcal", nullable = false)
    private BigDecimal totalKcal;

    @Column(name = "cooked_at", nullable = false, updatable = false)
    private OffsetDateTime cookedAt;

    @PrePersist
    protected void onCreate() {
        cookedAt = OffsetDateTime.now();
    }
}