package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.MealPlanEntry;
import com.humanworkstream.cooked.enumeration.MealSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealPlanRepository extends JpaRepository<MealPlanEntry, Long> {

    List<MealPlanEntry> findByUserIdAndPlanDateBetweenOrderByPlanDateAscCreatedAtAsc(
            Long userId, LocalDate from, LocalDate to);

    Optional<MealPlanEntry> findByUserIdAndPlanDateAndMealSlotAndRecipeId(
            Long userId, LocalDate planDate, MealSlot mealSlot, Long recipeId);
}
