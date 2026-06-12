package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.entity.id.RecipeInstructionId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_instruction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeInstruction {

    @EmbeddedId
    private RecipeInstructionId id;

    @Column(nullable = false)
    private String instruction;
}