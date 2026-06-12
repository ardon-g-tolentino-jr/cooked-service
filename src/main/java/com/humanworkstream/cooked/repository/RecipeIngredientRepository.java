package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.RecipeIngredient;
import com.humanworkstream.cooked.entity.id.RecipeIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, RecipeIngredientId> {

    List<RecipeIngredient> findByIdRecipeId(Long recipeId);

    void deleteByIdRecipeId(Long recipeId);
}