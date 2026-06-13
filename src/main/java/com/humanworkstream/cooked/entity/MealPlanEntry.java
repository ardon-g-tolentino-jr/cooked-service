package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.enumeration.MealSlot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meal_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    // converted to/from its lowercase TEXT value by MealSlotConverter (autoApply)
    @Column(name = "meal_slot", nullable = false)
    private MealSlot mealSlot;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
