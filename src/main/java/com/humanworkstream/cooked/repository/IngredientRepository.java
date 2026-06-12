package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

    List<Ingredient> findByRecipeIdOrderByPositionAsc(Integer recipeId);

    void deleteByRecipeId(Integer recipeId);
}