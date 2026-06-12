package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "recipe_id", nullable = false)
    private Integer recipeId;

    @Column(nullable = false)
    private String name;

    // Free-form amount, e.g. "2", "1/2", "a pinch"
    @Column
    private String quantity;

    @Column
    private String unit;

    @Column
    private Integer position;
}