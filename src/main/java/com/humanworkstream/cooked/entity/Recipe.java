package com.humanworkstream.cooked.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "recipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // NULL for built-in / community-seed recipes
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    // Display author when owner is NULL (e.g. "Chef Maria" or "@handle")
    @Column(name = "author_label")
    private String authorLabel;

    // FK to cuisine(name); validated in service layer
    @Column(nullable = false)
    private String cuisine;

    @Column(name = "prep_time_min", nullable = false)
    private Integer prepTimeMin;

    @Column(nullable = false)
    private Integer servings;

    @Column(name = "is_community", nullable = false)
    private Boolean isCommunity;

    // User can publish their recipe to the community feed
    @Column(name = "is_shared", nullable = false)
    private Boolean isShared;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (isCommunity == null) isCommunity = false;
        if (isShared == null) isShared = false;
    }
}