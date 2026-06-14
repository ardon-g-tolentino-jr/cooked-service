package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.entity.id.PantryTemplateItemId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pantry_template_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PantryTemplateItem {

    @EmbeddedId
    private PantryTemplateItemId id;
}
