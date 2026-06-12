package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.entity.id.RecipeMoodId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_mood")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMood {

    @EmbeddedId
    private RecipeMoodId id;
}