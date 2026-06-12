package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.entity.id.UserIngredientEditId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "user_ingredient_edit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIngredientEdit {

    @EmbeddedId
    private UserIngredientEditId id;

    @Column(name = "kcal_per_gram")
    private BigDecimal kcalPerGram;

    @Column(name = "grams_per_piece")
    private BigDecimal gramsPerPiece;
}