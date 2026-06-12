package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.entity.id.SavedRecipeId;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "saved_recipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedRecipe {

    @EmbeddedId
    private SavedRecipeId id;

    @Column(name = "saved_at", nullable = false, updatable = false)
    private OffsetDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        savedAt = OffsetDateTime.now();
    }
}