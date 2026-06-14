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
public class PantryTemplateItemId implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "ingredient_id")
    private Long ingredientId;
}
