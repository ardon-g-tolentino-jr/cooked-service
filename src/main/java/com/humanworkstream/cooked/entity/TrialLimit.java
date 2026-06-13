package com.humanworkstream.cooked.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Admin-configurable access limit for one gated component, applied to trial users. */
@Entity
@Table(name = "trial_limit")
@Data
@NoArgsConstructor
public class TrialLimit {

    @Id
    @Column(name = "component")
    private String component;

    @Column(name = "access_enabled", nullable = false)
    private boolean accessEnabled = true;

    // NULL = unlimited
    @Column(name = "max_count")
    private Integer maxCount;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
