package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.Ingredient;
import com.humanworkstream.cooked.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public List<Ingredient> findByRecipeId(Integer recipeId) {
        log.info("[IngredientService] Querying ingredients recipeId={}", recipeId);
        return ingredientRepository.findByRecipeIdOrderByPositionAsc(recipeId);
    }

    @Transactional
    public Ingredient create(Ingredient ingredient) {
        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("[IngredientService] Created ingredient id={} recipeId={} name={}",
                saved.getId(), saved.getRecipeId(), saved.getName());
        return saved;
    }

    @Transactional
    public Optional<Ingredient> patch(Integer id, Ingredient patch) {
        log.info("[IngredientService] Patching ingredient id={}", id);
        Optional<Ingredient> existing = ingredientRepository.findById(id);
        if (existing.isEmpty()) {
            log.warn("[IngredientService] Ingredient not found for patch id={}", id);
            return Optional.empty();
        }
        return existing.map(e -> {
            if (patch.getName() != null) e.setName(patch.getName());
            if (patch.getQuantity() != null) e.setQuantity(patch.getQuantity());
            if (patch.getUnit() != null) e.setUnit(patch.getUnit());
            if (patch.getPosition() != null) e.setPosition(patch.getPosition());
            return ingredientRepository.save(e);
        });
    }

    @Transactional
    public void delete(Integer id) {
        log.info("[IngredientService] Deleting ingredient id={}", id);
        ingredientRepository.deleteById(id);
    }
}