package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.RecipeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipeRatingRepository extends JpaRepository<RecipeRating, RecipeRating.PK> {

    Optional<RecipeRating> findByRecipeIdAndUserId(Long recipeId, Long userId);

    long countByRecipeId(Long recipeId);

    @Query("SELECT AVG(r.stars) FROM RecipeRating r WHERE r.recipeId = :recipeId")
    Double avgByRecipeId(@Param("recipeId") Long recipeId);
}
