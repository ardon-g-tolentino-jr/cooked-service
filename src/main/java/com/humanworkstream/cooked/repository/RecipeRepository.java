package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // My recipes + any community/shared recipes
    @Query("SELECT r FROM Recipe r WHERE r.ownerUserId = :userId OR r.isCommunity = true ORDER BY r.name ASC")
    List<Recipe> findVisibleToUser(@Param("userId") Long userId);

    List<Recipe> findByOwnerUserIdOrderByNameAsc(Long ownerUserId);
}