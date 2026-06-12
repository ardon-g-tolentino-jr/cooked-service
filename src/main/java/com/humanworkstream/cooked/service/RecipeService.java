package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.Recipe;
import com.humanworkstream.cooked.repository.IngredientRepository;
import com.humanworkstream.cooked.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public List<Recipe> findByUserId(Integer userId) {
        log.info("[RecipeService] Querying recipes userId={}", userId);
        return recipeRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Recipe> findById(Integer id) {
        log.info("[RecipeService] Querying recipe id={}", id);
        Optional<Recipe> result = recipeRepository.findById(id);
        if (result.isEmpty()) {
            log.warn("[RecipeService] Recipe not found id={}", id);
        }
        return result;
    }

    @Transactional
    public Recipe create(Recipe recipe) {
        Recipe saved = recipeRepository.save(recipe);
        log.info("[RecipeService] Created recipe id={} name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Optional<Recipe> patch(Integer id, Recipe patch) {
        log.info("[RecipeService] Patching recipe id={}", id);
        Optional<Recipe> existing = recipeRepository.findById(id);
        if (existing.isEmpty()) {
            log.warn("[RecipeService] Recipe not found for patch id={}", id);
            return Optional.empty();
        }
        return existing.map(e -> {
            if (patch.getName() != null) e.setName(patch.getName());
            if (patch.getDescription() != null) e.setDescription(patch.getDescription());
            if (patch.getCuisine() != null) e.setCuisine(patch.getCuisine());
            if (patch.getDifficulty() != null) e.setDifficulty(patch.getDifficulty());
            if (patch.getPrepMinutes() != null) e.setPrepMinutes(patch.getPrepMinutes());
            if (patch.getCookMinutes() != null) e.setCookMinutes(patch.getCookMinutes());
            if (patch.getServings() != null) e.setServings(patch.getServings());
            if (patch.getInstructions() != null) e.setInstructions(patch.getInstructions());
            return recipeRepository.save(e);
        });
    }

    @Transactional
    public void delete(Integer id) {
        log.info("[RecipeService] Deleting recipe id={}", id);
        ingredientRepository.deleteByRecipeId(id);
        recipeRepository.deleteById(id);
    }
}
