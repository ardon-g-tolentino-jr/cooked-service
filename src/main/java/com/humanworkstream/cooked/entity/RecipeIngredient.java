package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.entity.id.RecipeIngredientId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recipe_ingredient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    @EmbeddedId
    private RecipeIngredientId id;

    // Grams per full recipe (before scaling)
    @Column(nullable = false)
    private BigDecimal grams;
}