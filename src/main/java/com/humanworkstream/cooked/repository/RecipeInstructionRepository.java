package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.RecipeInstruction;
import com.humanworkstream.cooked.entity.id.RecipeInstructionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeInstructionRepository extends JpaRepository<RecipeInstruction, RecipeInstructionId> {

    List<RecipeInstruction> findByIdRecipeIdOrderByIdStepNoAsc(Long recipeId);

    void deleteByIdRecipeId(Long recipeId);
}