package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cook_history_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookHistoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cook_history_id", nullable = false)
    private Long cookHistoryId;

    // Snapshot of ingredient name at cook time; survives ingredient deletion
    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Column(nullable = false)
    private BigDecimal grams;

    @Column(nullable = false)
    private BigDecimal kcal;
}