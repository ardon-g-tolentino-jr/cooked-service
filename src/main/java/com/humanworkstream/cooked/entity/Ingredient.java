package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ingredient")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "kcal_per_gram", nullable = false)
    private BigDecimal kcalPerGram;

    // NULL unless countable (pcs)
    @Column(name = "grams_per_piece")
    private BigDecimal gramsPerPiece;

    @Column(name = "is_builtin", nullable = false)
    private Boolean isBuiltin;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (isBuiltin == null) isBuiltin = false;
    }
}