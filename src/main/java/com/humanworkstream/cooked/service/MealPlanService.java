package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.dto.MealPlanAddRequest;
import com.humanworkstream.cooked.dto.MealPlanEntryResponse;
import com.humanworkstream.cooked.entity.MealPlanEntry;
import com.humanworkstream.cooked.entity.Recipe;
import com.humanworkstream.cooked.repository.MealPlanRepository;
import com.humanworkstream.cooked.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final TrialLimitService trialLimits;

    @Transactional(readOnly = true)
    public List<MealPlanEntryResponse> list(Long userId, LocalDate from, LocalDate to) {
        trialLimits.assertEnabled(TrialLimitService.MEAL_PLAN);
        List<MealPlanEntry> entries = mealPlanRepository
                .findByUserIdAndPlanDateBetweenOrderByPlanDateAscCreatedAtAsc(userId, from, to);
        Map<Long, String> names = recipeRepository
                .findAllById(entries.stream().map(MealPlanEntry::getRecipeId).toList())
                .stream().collect(Collectors.toMap(Recipe::getId, Recipe::getName));
        return entries.stream()
                .map(e -> MealPlanEntryResponse.from(e, names.getOrDefault(e.getRecipeId(), "Unknown")))
                .toList();
    }

    @Transactional
    public MealPlanEntryResponse add(Long userId, MealPlanAddRequest req) {
        trialLimits.assertEnabled(TrialLimitService.MEAL_PLAN);
        Recipe recipe = recipeRepository.findById(req.recipeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown recipe"));
        MealPlanEntry entry = mealPlanRepository
                .findByUserIdAndPlanDateAndMealSlotAndRecipeId(userId, req.planDate(), req.mealSlot(), req.recipeId())
                .orElseGet(() -> mealPlanRepository.save(
                        new MealPlanEntry(null, userId, req.planDate(), req.mealSlot(), req.recipeId(), null)));
        log.info("[MealPlanService] Planned recipeId={} date={} slot={} userId={}",
                req.recipeId(), req.planDate(), req.mealSlot(), userId);
        return MealPlanEntryResponse.from(entry, recipe.getName());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        trialLimits.assertEnabled(TrialLimitService.MEAL_PLAN);
        MealPlanEntry entry = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan entry not found"));
        if (!userId.equals(entry.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        mealPlanRepository.deleteById(id);
        log.info("[MealPlanService] Removed mealPlanId={} userId={}", id, userId);
    }
}
