package com.humanworkstream.cooked.service;

import com.humanworkstream.cooked.entity.SavedRecipe;
import com.humanworkstream.cooked.entity.id.SavedRecipeId;
import com.humanworkstream.cooked.repository.RecipeRepository;
import com.humanworkstream.cooked.repository.SavedRecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedRecipeService {

    private final SavedRecipeRepository savedRecipeRepository;
    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public List<Long> listSavedIds(Long userId) {
        return savedRecipeRepository.findRecipeIdsByUserId(userId);
    }

    @Transactional
    public void save(Long userId, Long recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }
        if (!savedRecipeRepository.existsByIdUserIdAndIdRecipeId(userId, recipeId)) {
            savedRecipeRepository.save(new SavedRecipe(new SavedRecipeId(userId, recipeId), null));
            log.info("[SavedRecipeService] Saved recipeId={} userId={}", recipeId, userId);
        }
    }

    @Transactional
    public void unsave(Long userId, Long recipeId) {
        savedRecipeRepository.deleteByIdUserIdAndIdRecipeId(userId, recipeId);
        log.info("[SavedRecipeService] Unsaved recipeId={} userId={}", recipeId, userId);
    }
}