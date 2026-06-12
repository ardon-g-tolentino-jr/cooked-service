package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // All built-ins plus ingredients created by this user
    List<Ingredient> findByIsBuiltinTrueOrCreatedByOrderByNameAsc(Long createdBy);

    boolean existsByNameIgnoreCase(String name);
}