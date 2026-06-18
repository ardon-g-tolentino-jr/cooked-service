package com.humanworkstream.cooked.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Composite key for {@code tier_limit}: (tier, component). */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TierLimitId implements Serializable {

    @Column(name = "tier")
    private String tier;

    @Column(name = "component")
    private String component;
}
