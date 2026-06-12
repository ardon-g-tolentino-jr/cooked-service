package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

    List<Recipe> findByUserIdOrderByNameAsc(Integer userId);
}