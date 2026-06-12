package com.humanworkstream.cooked.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RecipeInstructionId implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "step_no")
    private Short stepNo;
}