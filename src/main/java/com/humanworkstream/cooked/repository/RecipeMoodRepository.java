package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.RecipeMood;
import com.humanworkstream.cooked.entity.id.RecipeMoodId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeMoodRepository extends JpaRepository<RecipeMood, RecipeMoodId> {

    List<RecipeMood> findByIdRecipeId(Long recipeId);

    void deleteByIdRecipeId(Long recipeId);
}