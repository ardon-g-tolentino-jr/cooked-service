package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.SavedRecipe;
import com.humanworkstream.cooked.entity.id.SavedRecipeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, SavedRecipeId> {

    @Query("SELECT sr.id.recipeId FROM SavedRecipe sr WHERE sr.id.userId = :userId")
    List<Long> findRecipeIdsByUserId(@Param("userId") Long userId);

    boolean existsByIdUserIdAndIdRecipeId(Long userId, Long recipeId);

    void deleteByIdUserIdAndIdRecipeId(Long userId, Long recipeId);
}