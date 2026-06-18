package com.humanworkstream.cooked.entity;

import com.humanworkstream.cooked.entity.id.TierLimitId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Admin-configurable access limit for one (tier, component), applied to users on that tier. */
@Entity
@Table(name = "tier_limit")
@Data
@NoArgsConstructor
public class TierLimit {

    @EmbeddedId
    private TierLimitId id;

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
